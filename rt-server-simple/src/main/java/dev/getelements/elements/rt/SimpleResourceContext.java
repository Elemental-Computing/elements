package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.provider.ExecutorServiceFactory;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;


public class SimpleResourceContext implements ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ExecutorService executorService;

    private volatile ExecutorServiceFactory<ExecutorService> executorServiceFactory;

    private final Lock lock = new ReentrantLock();

    @Override
    public void start() {
        try (var monitor = Monitor.enter(lock)) {
            if (executorService == null) {
                executorService = getExecutorServiceFactory().getService(SimpleIndexContext.class);
                getResourceService().start();
            } else {
                throw new IllegalStateException("Already running.");
            }
        }
    }

    @Override
    public void stop() {
        try (var monitor = Monitor.enter(lock)) {
            if (executorService == null) {
                throw new IllegalStateException("Not running.");
            } else {

                executorService.shutdown();

                try {
                    if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                        logger.error("Timed out.");
                    }
                } catch (InterruptedException ex) {
                    logger.error("Interrupted");
                } finally {
                    executorService = null;
                }

                getResourceService().stop();

            }
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) throw new IllegalStateException("Not running.");
        return executorService;
    }

    @Override
    public ResourceId createAttributes(final String module, final Path path,
                                       final Attributes attributes, final Object... args) {
        logger.debug("Loading module {} -> {}", module, path);
        final Resource resource = getResourceLoader().load(module, attributes, args);
        getResourceService().addAndReleaseResource(path, resource);
        return resource.getId();
    }

    @Override
    public void createAttributesAsync(final Consumer<ResourceId> success, final Consumer<Throwable> failure,
                                      final String module, final Path path, final Attributes attributes, final Object... args) {
        getExecutorService().submit(() -> {
            try {
                final ResourceId resourceId = createAttributes(module, path, attributes, args);
                success.accept(resourceId);
                return resourceId;
            } catch (Throwable th) {
                logger.error("Caught Exception loading module {} -> {}", module, path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public void destroyAsync(final Consumer<Void> success,
                             final Consumer<Throwable> failure,
                             final ResourceId resourceId) {
        // The Resource must be locked in order to properly destroy it because it invovles mutating the Resource itself.
        // if we try to destroy it without using the scheduler, we could end up with two threads accessing it at the
        // same time, which is no good.
        getScheduler().performV(resourceId, r -> {
            try {
                getResourceService().destroy(resourceId);
                success.accept(null);
            } catch (Throwable throwable) {
                failure.accept(throwable);
            }
        }, failure.andThen(th -> logger.error("Failure", th)));
    }

    @Override
    public void invokeAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                            final ResourceId resourceId, final String method, final Object... args) {
        getScheduler().perform(resourceId, resource -> doInvoke(success, failure, resource, method, args), failure);
    }

    @Override
    public void invokePathAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                                final Path path, final String method, final Object... args) {
        getScheduler().perform(path, resource -> doInvoke(success, failure, resource, method, args), failure);
    }

    private TaskId doInvoke(final Consumer<Object> success, final Consumer<Throwable> failure,
                            final ResourceService.ResourceTransaction txn, final String method, final Object... args) {
        try {
            return txn.getResource().getMethodDispatcher(method).params(args).dispatch(success, failure);
        } catch (Throwable th) {
            failure.accept(th);
            throw th;
        }
    }

    @Override
    public void destroyAllResources() {
        getResourceService().removeAndCloseAllResources();
    }

    @Override
    public void destroyAllResourcesAsync(Consumer<Void> success, Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {

            try {
                getResourceService().removeAndCloseAllResources();
                success.accept(null);
            } catch (Throwable th) {
                failure.accept(th);
                throw th;
            }

            return null;
        });
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

    public ExecutorServiceFactory<ExecutorService> getExecutorServiceFactory() {
        return executorServiceFactory;
    }

    @Inject
    public void setExecutorServiceFactory(@Named(Instance.EXECUTOR_SERVICE) ExecutorServiceFactory<ExecutorService> executorServiceFactory) {
        this.executorServiceFactory = executorServiceFactory;
    }

}
