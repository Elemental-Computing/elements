package dev.getelements.elements.rt.remote;

import javolution.io.Struct;

import java.util.function.Consumer;

/**
 * Prepends a message request, providing scope and other information about how to dispatch the invocation.
 */
public class RequestHeader extends Struct {

    /**
     * Indicates the number of additional {@link Consumer} instances which will be passed to
     * the remote invocation dispatcher.  This corresponds to the {@link ResponseHeader#part} field.
     */
    public final Signed32 additionalParts = new Signed32();

}
