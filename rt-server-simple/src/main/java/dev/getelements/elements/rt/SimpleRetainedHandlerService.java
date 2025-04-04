package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.cluster.path.Path.fromComponents;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

public class SimpleRetainedHandlerService implements RetainedHandlerService {

    private static final int PURGE_BATCH_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(SimpleRetainedHandlerService.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private final AtomicBoolean running = new AtomicBoolean();

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    private void purge() {

        final Path path = fromComponents("tmp", "handler", "re", "*");

        List<ResourceService.Unlink> unlinkList;

        do {
            unlinkList = getResourceService().unlinkMultiple(path, PURGE_BATCH_SIZE);
            logger.info("Purged {} resources.", unlinkList.size());
            logger.debug("Purged [{}]", unlinkList);
        } while (!unlinkList.isEmpty());

    }

    @Override
    public TaskId perform(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final long timeout, final TimeUnit timeoutUnit,
            final String module, final Attributes attributes,
            final String method, final Object... args) {

        final var path = fromComponents("tmp", "handler", "re", randomUUID().toString());

        try (var acquisition = acquire(path, module, attributes)) {

            final var resourceId = acquisition.getResourceId();
            final var unlink = getScheduler().scheduleUnlink(path, timeout, timeoutUnit);

            final AtomicBoolean sent = new AtomicBoolean();

            final Consumer<Throwable> _failure = t -> {
                try {

                    final String _args = stream(args)
                            .map(a -> a == null ? "null" : a.toString())
                            .collect(Collectors.joining(","));

                    logger.error("Caught exception processing retained handler {}.{}({}).", module, method, _args, t);
                    if (sent.compareAndSet(false, true)) failure.accept(t);

                } catch (Exception ex) {
                    logger.error("Caught exception destroying resource {}", resourceId, ex);
                } finally {
                    unlink.run();
                }
            };

            final Consumer<Object> _success = o -> {
                try {
                    if (sent.compareAndSet(false, true)) success.accept(o);
                } catch (Throwable th) {
                    _failure.accept(th);
                } finally {
                    unlink.run();
                }
            };

            try (var txn = acquisition.begin()) {
                return txn
                        .getResource()
                        .getMethodDispatcher(method)
                        .params(args)
                        .dispatch(_success, _failure);
            }

        }

    }

    private ResourceService.ResourceAcquisition acquire(final Path path, final String module, final Attributes attributes) {
        final Resource resource = getResourceLoader().load(module, attributes);
        return getResourceService().addAndAcquireResource(path, resource);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
