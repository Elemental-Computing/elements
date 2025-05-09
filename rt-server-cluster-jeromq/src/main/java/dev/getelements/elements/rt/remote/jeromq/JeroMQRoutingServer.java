package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.rt.jeromq.JeroMQMonitorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BooleanSupplier;

import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.EXCEPTION;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_ERROR;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static zmq.ZError.EAGAIN;

public class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private long nextLog = 0;

    private final Logger logger;

    private static final long LOG_INTERVAL = 5000;

    private static final long POLL_TIMEOUT_MILLISECONDS = 1000;

    private final ZContext zContextShadow;

    private final ZMQ.Poller poller;

    private final JeroMQCommandServer control;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    private final JeroMQMonitorThread monitorThread;

    private final JeroMQSecurity securityChain;

    public JeroMQRoutingServer(final InstanceId instanceId,
                               final ZContext zContext,
                               final List<String> bindAddresses,
                               final JeroMQSecurity securityChain) {

        this.securityChain = securityChain;
        this.logger = getLogger(instanceId);
        this.zContextShadow = shadow(zContext);
        this.poller = zContextShadow.createPoller(0);

        final var main = securityChain.server(() -> zContextShadow.createSocket(ROUTER));

        bindAddresses.forEach(addr -> {
            logger.info("Binding on {} - ", addr);
            main.bind(addr);
        });

        final var frontendIndex = poller.register(main, POLLIN | POLLERR);
        final var frontend = poller.getItem(frontendIndex);

        this.multiplex = new JeroMQMultiplexRouter(instanceId, zContextShadow, poller);
        this.demultiplex = new JeroMQDemultiplexRouter(instanceId, zContextShadow, poller, frontend);
        this.control = new JeroMQCommandServer(instanceId, frontend, multiplex, demultiplex);
        this.monitorThread = new JeroMQMonitorThread(JeroMQRoutingServer.class.getSimpleName(), logger, zContext, main);
        this.monitorThread.start();

    }

    public void run(final BooleanSupplier running) {
        while (running.getAsBoolean()) {

            if (poller.poll(POLL_TIMEOUT_MILLISECONDS) < 0 || currentThread().isInterrupted()) {
                logger.info("Poller signaled interruption.  Exiting.");
                break;
            }

            try {
                control.poll();
                multiplex.poll();
                demultiplex.poll();
            } catch (Exception ex) {
                logger.error("Caught exception in routing server.", ex);
            }

            final var size = poller.getSize();

            for (var index = 0; index < size; ++index) {
                final var item = poller.getItem(index);
                if (item == null) continue;
                final var err = item.getSocket().errno();
                if (err != 0 && err != EAGAIN) logger.error("Socket got errno: {}", err);
            }

            final long now = currentTimeMillis();

            if (nextLog <= now) {
                control.log();
                multiplex.log();
                demultiplex.log();
                nextLog = now + LOG_INTERVAL;
            }

        }

    }

    @Override
    public void close() {

        poller.close();
        zContextShadow.close();

        try {
            monitorThread.interrupt();
            monitorThread.join();
        } catch (InterruptedException ex) {
            logger.error("Interrupted while closing monitor thread.", ex);
        }

    }

    public static ZMsg error(final JeroMQControlResponseCode code, final String message) {
        final var response = new ZMsg();
        (code == null ? UNKNOWN_ERROR : code).pushResponseCode(response);
        response.addLast(message.getBytes(CHARSET));
        return response;
    }

    public static ZMsg exceptionError(final Logger logger, final Exception ex) {
        return exceptionError(logger, EXCEPTION, ex);
    }

    public static ZMsg exceptionError(final Logger logger, final JeroMQControlResponseCode code, final Exception ex) {

        logger.error("Exception processing request.", ex);
        final var response = new ZMsg();

        code.pushResponseCode(response);
        response.addLast(ex.getMessage().getBytes(CHARSET));

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(ex);
            }

            response.addLast(bos.toByteArray());

        } catch (IOException e) {
            logger.error("Caught exception serializing exception.", e);
        }

        return response;

    }

    private static Logger getLogger(final InstanceId instanceId) {
        return getLogger(JeroMQRoutingServer.class, instanceId);
    }

    public static Logger getLogger(final Class<?> componentClass, final InstanceId instanceId) {
        return LoggerFactory.getLogger(format("%s.%s.%s",
            JeroMQRoutingServer.class.getSimpleName(),
            componentClass.getSimpleName(),
            instanceId.asString()));
    }

}
