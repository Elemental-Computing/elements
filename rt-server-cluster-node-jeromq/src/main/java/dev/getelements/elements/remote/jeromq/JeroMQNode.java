package dev.getelements.elements.remote.jeromq;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceBinding;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.NodeLifecycle;
import dev.getelements.elements.rt.remote.NodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.getelements.elements.rt.AsyncConnection.Event.ERROR;
import static dev.getelements.elements.rt.AsyncConnection.Event.READ;
import static dev.getelements.elements.rt.remote.NodeState.*;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.zeromq.SocketType.*;
import static org.zeromq.ZMQ.Socket;

public class JeroMQNode implements Node {

    private static final Logger staticLogger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String OUTBOUND_ADDR_FORMAT = "inproc://node/%s/out";

    public static final String JEROMQ_NODE_MIN_CONNECTIONS = "dev.getelements.elements.remote.jeromq.node.min.connections";

    public static final String JEROMQ_NODE_MAX_CONNECTIONS = "dev.getelements.elements.remote.jeromq.node.max.connections";

    private final AtomicReference<NodeState> state = new AtomicReference<>(READY);

    private final AtomicReference<NodeContext> context = new AtomicReference<>();

    private String name;

    private NodeId nodeId;

    private int minConnections;

    private int maxConnections;

    private LocalInvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private NodeLifecycle nodeLifecycle;

    private AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public NodeState getState() {
        return state.get();
    }

    public String getOutboundAddr() {
        return format(OUTBOUND_ADDR_FORMAT, getNodeId().asString());
    }

    @Override
    public Startup beginStartup() {

        final var c = new NodeContext();

        c.logger.info("Beginning startup.");

        if (!state.compareAndSet(READY, STARTING) || !context.compareAndSet(null, c)) {
            throw new IllegalStateException("Already started.");
        }

        return new Startup() {

            @Override
            public Node getNode() {
                return JeroMQNode.this;
            }

            @Override
            public void preStart() {
                try {
                    check();
                    c.logger.info("Issuing pre-start command.");
                    getNodeLifecycle().nodePreStart(getNode());
                } catch (Exception ex) {
                    state.set(UNHEALTHY);
                    c.logger.error("Caught excpetion issuing pre-start command.", ex);
                    throw ex;
                }
            }

            @Override
            public void start(final InstanceBinding binding) {
                try {
                    check();
                    c.logger.info("Issuing start command with binding {}.", binding);
                    c.start(binding);
                    state.set(STARTED);
                } catch (Exception ex) {
                    state.set(UNHEALTHY);
                    c.logger.error("Caught exception issuing start command.", ex);
                    throw ex;
                }
            }

            @Override
            public void postStart() {
                try {
                    check();
                    c.logger.info("Issuing post-start command for node {}", nodeId);
                    getNodeLifecycle().nodePostStart(getNode());
                    state.set(HEALTHY);
                } catch (Exception ex) {
                    state.set(UNHEALTHY);
                    c.logger.error("Caught exception issuing post-start command.  Terminating node.", ex);
                    throw ex;
                }
            }

            @Override
            public void cancel() {

                try {
                    c.stop();
                } catch (Exception ex) {
                    c.logger.error("Error Canceling startup.", ex);
                }

                if (!context.compareAndSet(c, null)) {
                    c.logger.error("Inconsistent state.  Startup does not reflect current state.");
                }

                state.set(READY);

            }

            private void check() {
                if (context.get() != c) throw new IllegalStateException("Incorrect/mismatched startup routine.");
            }

        };
    }

    @Override
    public Shutdown beginShutdown() {

        final var c = context.getAndSet(null);
        final var logger = c == null ? staticLogger : c.logger;

        return new Shutdown() {

            @Override
            public void preStop() {
                try {
                    logger.info("Issuing NodeLifecycle pre-stop command.");
                    getNodeLifecycle().nodePreStop(JeroMQNode.this);
                    state.set(STOPPING);
                } catch (Exception ex) {
                    logger.error("Caught exception issuing pre-stop command.", ex);
                }
            }

            @Override
            public void stop() {
                try {
                    if (c != null) c.stop();
                    logger.info("Shutdown.  Issuing NodeLifecycle post-stop command.");
                    state.set(STOPPED);
                } catch (Exception ex) {
                    logger.error("Caught exception issuing stop command.", ex);
                }
            }

            @Override
            public void postStop() {
                try {
                    logger.info("Shutdown.  Issued NodeLifecycle stop command.");
                    getNodeLifecycle().nodePostStop(JeroMQNode.this);
                    state.set(READY);
                } catch (Exception ex) {
                    logger.error("Caught excpetion issuing post-stop command.", ex);
                }
            }
        };
    }

    private Logger loggerForNode() {
        final String prefix = JeroMQNode.class.getName();
        if (getName() != null) return LoggerFactory.getLogger(format("%s.%s", prefix, getName()));
        else if (getNodeId() != null) return LoggerFactory.getLogger(format("%s.%s", prefix, getNodeId().asString()));
        else return staticLogger;
    }

    public LocalInvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(LocalInvocationDispatcher invocationDispatcher) {
        this.invocationDispatcher = invocationDispatcher;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    @Inject
    public void setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public PayloadWriter getPayloadWriter() {
        return payloadWriter;
    }

    @Inject
    public void setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
    }

    public AsyncConnectionService<ZContext, ZMQ.Socket> getAsyncConnectionService() {
        return asyncConnectionService;
    }

    @Inject
    public void setAsyncConnectionService(AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService) {
        this.asyncConnectionService = asyncConnectionService;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Inject
    public void setName(@Named(NAME) String name) {
        this.name = name;
    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(JEROMQ_NODE_MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(JEROMQ_NODE_MAX_CONNECTIONS) int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public NodeLifecycle getNodeLifecycle() {
        return nodeLifecycle;
    }

    @Inject
    public void setNodeLifecycle(NodeLifecycle nodeLifecycle) {
        this.nodeLifecycle = nodeLifecycle;
    }

    private class NodeContext {

        private final Logger logger = loggerForNode();

        private AsyncConnection<ZContext, ZMQ.Socket> frontendConnection;

        private AsyncConnectionGroup<ZContext, ZMQ.Socket> mainConnectionGroup;

        private AsyncConnectionPool<ZContext, ZMQ.Socket> outboundConnectionPool;

        private final AtomicInteger dispatcherCount = new AtomicInteger();

        private final ExecutorService dispatchExecutorService = newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(format("%s %s.in #%d", getClass().getSimpleName(), getName(), dispatcherCount.incrementAndGet()));
            thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
            return thread;
        });

        public void start(final InstanceBinding instanceBinding) {

            final CountDownLatch latch = new CountDownLatch(3);

            outboundConnectionPool = getAsyncConnectionService().allocatePool(
                "JeroMQNode Outbound",
                getMinConnections(),
                getMaxConnections(),
                z -> {
                    final Socket socket = z.createSocket(PUSH);
                    socket.connect(getOutboundAddr());
                    return socket;
                });

            getAsyncConnectionService().group(format("%s %s", JeroMQNode.class.getSimpleName(), name))
                .connection(z -> {
                    final var socket = z.createSocket(ROUTER);
                    socket.bind(instanceBinding.getBindAddress());
                    return socket;
                }, connection -> {
                    frontendConnection = connection;
                    connection.setEvents(READ, ERROR);
                    connection.onRead(this::onFrontendRead);
                    connection.onError(this::onFrontendError);
                    latch.countDown();
                })
                .connection(z -> {
                    final Socket socket = z.createSocket(PULL);
                    socket.bind(getOutboundAddr());
                    return socket;
                }, connection -> {
                    connection.setEvents(READ, ERROR);
                    connection.onRead(this::onBackendRead);
                    connection.onError(this::onBackendError);
                    latch.countDown();
                }).build(group -> {
                    mainConnectionGroup = group;
                    latch.countDown();
                });

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void onFrontendRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            final ZMsg msg = ZMsg.recvMsg(connection.socket());
            dispatchExecutorService.submit(() -> dispatch(msg));
        }

        private void onFrontendError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            state.set(UNHEALTHY);
            logger.error("Frontend Connection Error {} - errno {}", connection, connection.socket().errno());
        }

        private void onBackendRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            final ZMsg msg = ZMsg.recvMsg(connection.socket());
            msg.send(frontendConnection.socket());
        }

        private void onBackendError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            state.set(UNHEALTHY);
            logger.error("Backend Connection Error {} - errno {}", connection, connection.socket().errno());
        }

        public void stop() {

            mainConnectionGroup.close();
            outboundConnectionPool.close();
            dispatchExecutorService.shutdownNow();

            try {
                if (!dispatchExecutorService.awaitTermination(10, MINUTES)) {
                    logger.error("Terminating dispatchers timed out.");
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node Thread Pool", e);
            }

        }

        private void dispatch(final ZMsg msg) {

            final JeroMQNodeInvocation invocation = new JeroMQNodeInvocation(
                msg,
                getInvocationDispatcher(),
                getPayloadReader(),
                getPayloadWriter(),
                outboundConnectionPool
            );

            invocation.dispatch();

        }

    }

}


