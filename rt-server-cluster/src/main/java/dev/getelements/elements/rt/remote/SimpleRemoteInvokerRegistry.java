package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.InstanceMetadata;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.exception.NodeNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceConnection;
import dev.getelements.elements.rt.util.ContextLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.remote.RemoteInvokerRegistrySnapshot.RefreshBuilder;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleRemoteInvokerRegistry implements RemoteInvokerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRemoteInvokerRegistry.class);

    private static final long SHUTDOWN_TIMEOUT = 1;

    private static final TimeUnit SHUTDOWN_UNITS = MINUTES;

    public static final String REFRESH_RATE_SECONDS = "dev.getelements.elements.rt.remote.invoker.registry.report.refresh.rate.seconds";

    public static final String REFRESH_TIMEOUT_SECONDS = "dev.getelements.elements.rt.remote.invoker.registry.report.refresh.timeout.seconds";

    public static final String TOTAL_REFRESH_TIMEOUT_SECONDS = "dev.getelements.elements.rt.remote.invoker.registry.report.total.refresh.timeout.seconds";

    public static final long DEFAULT_REFRESH_RATE = 5;

    public static final long DEFAULT_REFRESH_TIMEOUT = 1;

    public static final long DEFAULT_TOTAL_REFRESH_TIMEOUT = 3;

    private InstanceId instanceId;

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private InstanceConnectionService instanceConnectionService;

    private final AtomicReference<RegistryContext> context = new AtomicReference<>();

    private long refreshRateSeconds;

    private long refreshTimeoutSeconds;

    private long totalRefreshTimeoutSeconds;

    @Override
    public void start() {

        final var context = new RegistryContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final RegistryContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public void refresh() {
        final RegistryContext context = getContext();
        context.refresh();
    }

    @Override
    public List<RemoteInvokerStatus> getAllRemoteInvokerStatuses() {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getAllRemoteInvokers();
    }

    @Override
    public List<RemoteInvokerStatus> getAllRemoteInvokerStatuses(ApplicationId applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getAllRemoteInvokerStatuses(applicationId);
    }

    @Override
    public RemoteInvokerStatus getBestRemoteInvokerStatus(ApplicationId applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getBestInvokerStatusForApplication(applicationId);
    }

    @Override
    public List<RemoteInvoker> getAllRemoteInvokers(final ApplicationId applicationId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        return snapshot.getAllRemoteInvokersForApplication(applicationId);
    }

    @Override
    public RemoteInvoker getRemoteInvoker(final NodeId nodeId) {
        final RemoteInvokerRegistrySnapshot snapshot = getSnapshot();
        final RemoteInvoker remoteInvoker = snapshot.getRemoteInvoker(nodeId);
        if (remoteInvoker == null) throw new NodeNotFoundException("No RemoteInvoker for: " + nodeId);
        return remoteInvoker;
    }

    private RegistryContext getContext() {
        final RegistryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private RemoteInvokerRegistrySnapshot getSnapshot() {
        final RegistryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context.snapshot;
    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(InstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public long getRefreshRateSeconds() {
        return refreshRateSeconds;
    }

    @Inject
    public void setRefreshRateSeconds(@Named(REFRESH_RATE_SECONDS) long refreshRateSeconds) {
        this.refreshRateSeconds = refreshRateSeconds;
    }

    public long getRefreshTimeoutSeconds() {
        return refreshTimeoutSeconds;
    }

    @Inject
    public void setRefreshTimeoutSeconds(@Named(REFRESH_TIMEOUT_SECONDS) long refreshTimeoutSeconds) {
        this.refreshTimeoutSeconds = refreshTimeoutSeconds;
    }

    public long getTotalRefreshTimeoutSeconds() {
        return totalRefreshTimeoutSeconds;
    }

    @Inject
    public void setTotalRefreshTimeoutSeconds(@Named(TOTAL_REFRESH_TIMEOUT_SECONDS) long totalRefreshTimeoutSeconds) {
        this.totalRefreshTimeoutSeconds = totalRefreshTimeoutSeconds;
    }

    private class RegistryContext {

        private Subscription connect;

        private Subscription disconnect;

        private ScheduledFuture<?> refreshScheduledFuture;

        private ScheduledExecutorService scheduledExecutorService;

        private final RemoteInvokerRegistrySnapshot snapshot = new RemoteInvokerRegistrySnapshot();

        private void start() {

            scheduledExecutorService = newSingleThreadScheduledExecutor(r -> {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName(SimpleRemoteInvokerRegistry.class.getSimpleName() + " refresher " + getInstanceId());
                thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Caught exception in {}", t, e)));
                return thread;
            });

            refreshScheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    refresh();
                } catch (Exception ex) {
                    logger.error("Could not refresh invoker registry.", ex);
                }
            }, 0, getRefreshRateSeconds(), SECONDS);

            connect = getInstanceConnectionService().subscribeToConnect(this::add);
            disconnect = getInstanceConnectionService().subscribeToDisconnect(this::remove);

            refresh();

        }

        private void stop() {

            connect.unsubscribe();
            disconnect.unsubscribe();
            refreshScheduledFuture.cancel(true);
            scheduledExecutorService.shutdown();

            try {
                scheduledExecutorService.awaitTermination(SHUTDOWN_TIMEOUT, SHUTDOWN_UNITS);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

            snapshot.clear();

        }

        private void add(final InstanceConnection connection) {

            // We schedule the async result to be on the scheduler thread to avoid blocking hte main.

            final var op = connection
                .getInstanceMetadataContext()
                .getInstanceMetadataAsync(
                    im -> scheduledExecutorService.submit(() -> add(connection, im)),
                    th -> logger.error("Failed to get instance metadata for {}", connection.getInstanceId(), th)
                );

            op.timeout(getRefreshTimeoutSeconds(), SECONDS);

        }

        private void add(final InstanceConnection connection, final InstanceMetadata instanceMetadata) {
            final var builder = snapshot.refresh();
            add(connection, instanceMetadata, builder).commit(this::cleanup);
        }

        private void cleanup(final RemoteInvoker ri, final Exception ex) {

            if (ex == null) {
                logger.info("Cleaning up {}", ri);
            } else {
                logger.error("Cleaning up {}", ri, ex);
            }

            if (ri != null) ri.stop();

        }

        private RefreshBuilder add(final InstanceConnection connection,
                                   final InstanceMetadata instanceMetadata,
                                   final RefreshBuilder builder) {

            final var quality = instanceMetadata.getQuality();
            final var nodeIdSet = instanceMetadata.getNodeIds();

            for (final NodeId nodeId : nodeIdSet) {
                builder.add(nodeId, quality, () -> establishNewConnection(nodeId, connection));
            }

            return builder;

        }

        private void remove(final InstanceConnection connection) {

            final var builder = snapshot.refresh();
            final var instanceId = connection.getInstanceId();

            builder.remove(instanceId).commit((ri, ex) -> {

                if (ex == null) {
                    logger.info("Cleaning up {}", ri);
                } else {
                    logger.error("Cleaning up {}", ri, ex);
                }

                if (ri != null) ri.stop();

            });

        }

        private void refresh() {

            final var operations = new ConcurrentLinkedQueue<Consumer<RefreshBuilder>>();
            final var connections = getInstanceConnectionService().getActiveConnections();
            final var latch = new ContextLatch(connections);

            for (final var connection : connections) {

                final var op = connection.getInstanceMetadataContext().getInstanceMetadataAsync(
                    im -> {
                        operations.add(b -> add(connection, im, b));
                        latch.finish(connection);
                    }, throwable -> {
                        operations.add(b -> b.remove(connection.getInstanceId()));
                        latch.finish(connection);
                    }
                );

                op.timeout(getRefreshTimeoutSeconds(), SECONDS);

            }

            try {
                if (latch.awaitFinish(getTotalRefreshTimeoutSeconds(), SECONDS)) {
                    final var builder = snapshot.refresh();
                    for (var op : operations) op.accept(builder);
                    builder.prune().commit(this::cleanup);
                } else {
                    logger.info("Timed out. Skipping refresh this cycle.");
                }
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        private RemoteInvoker establishNewConnection(final NodeId nodeId, final InstanceConnection connection) {
            final String addr = connection.openRouteToNode(nodeId);
            final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
            logger.info("Connecting to node {} via address {}", nodeId, addr);
            remoteInvoker.start(addr);
            return remoteInvoker;
        }

    }

}
