package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.exception.HandlerTimeoutException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.util.SyncWait;
import dev.getelements.elements.sdk.Attributes;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * Used to manage handler-type {@link Resource} instances.  These are intended to be short-lived and managed entirely
 * on the server side.  Unlike using the bare {@link ResourceContext} the methods in this interface will automatically
 * guaranteed the lifecycle of the remote {@link Resource}.
 */
@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
public interface HandlerContext {

    /**
     * The timeout for the {@link HandlerContext} in milliseconds.
     */
    String HANDLER_TIMEOUT_MSEC = "dev.getelements.elements.rt.handler.timeout.msec";

    /**
     * Starts this {@link HandlerContext}.
     */
    default void start() {}

    /**
     * Stops this {@link HandlerContext}.
     */
    default void stop() {}

    /**
     * Synchronously invokes {@link #invokeSingleUseHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)}
     * blocking until the call returns.
     *
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     * @return the invocation result
     */
    default Object invokeSingleUseHandler(
            @Serialize final Attributes attributes,
            @Serialize final String module, @Serialize final String method,
            @Serialize final Object ... args) {
        final SyncWait<Object> resultSyncWait = new SyncWait<>(getClass());
        invokeSingleUseHandlerAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer(),
                                    attributes, module, method, args);
        return resultSyncWait.get();
    }

    /**
     * Creates a {@link Resource}, invokes a method on that {@link Resource}, and returns the result as either a
     * success or a failure.  The {@link HandlerContext} will assign a temporary {@link Path}, and {@link ResourceId}
     * which will not be exposed to the client code.
     *
     * Further, this will guaranteed the destruction of the created {@link Resource} even if the client abandons the
     * call this method.
     *
     * The underlying {@link HandlerContext} may time out the {@link Resource}.  In which case the caller will receive
     * an instance fo {@link HandlerTimeoutException} through the returned {@link Future} or in the
     * {@link Consumer<Throwable>} handed to the method.
     *
     * @param success invoked when the invocation successfully returns
     * @param failure invoked whent he invocation encounters an error
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     */
    @RemotelyInvokable
    void invokeSingleUseHandlerAsync(
        @ResultHandler Consumer<Object> success, @ErrorHandler Consumer<Throwable> failure,
        @Serialize Attributes attributes, @Serialize String module,
        @Serialize String method, @Serialize Object ... args);

    /**
     * Synchronously invokes {@link #invokeRetainedHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)}
     * blocking until the call returns.
     *
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     * @return the invocation result
     */
    default Object invokeRetainedHandler(
            @Serialize final Attributes attributes,
            @Serialize final String module, @Serialize final String method,
            @Serialize final Object ... args) {
        final SyncWait<Object> resultSyncWait = new SyncWait<>(getClass());
        invokeRetainedHandlerAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer(),
                                   attributes, module, method, args);
        return resultSyncWait.get();
    }

    /**
     * Creates a {@link Resource}, invokes a method on that {@link Resource}, and returns the result as either a
     * success or a failure.  The {@link HandlerContext} will assign a temporary {@link Path}, and {@link ResourceId}
     * which will not be exposed to the client code.
     *
     * Unlike {@link #invokeSingleUseHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)} this will
     * just unlink the {@link Resource} after it invokes the specified method.  It will be responsiblity of the invoked
     * method to provide further retentions should it be desired.
     *
     * The underlying {@link HandlerContext} may time out the {@link Resource}.  In which case the caller will receive
     * an instance fo {@link HandlerTimeoutException} through the returned {@link Future} or in the
     * {@link Consumer<Throwable>} handed to the method.
     *
     * @param success invoked when the invocation successfully returns
     * @param failure invoked whent he invocation encounters an error
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     */
    @RemotelyInvokable
    void invokeRetainedHandlerAsync(
        @ResultHandler Consumer<Object> success, @ErrorHandler Consumer<Throwable> failure,
        @Serialize Attributes attributes, @Serialize String module,
        @Serialize String method, @Serialize Object ... args);

}
