package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;

/**
 * Runs a local instance of Elements suitable for debugging.
 */
public interface ElementsLocal extends AutoCloseable {

    /**
     * Starts this {@link ElementsLocal}.
     *
     * @return this instance
     */
    ElementsLocal start();

    /**
     * Runs the instance and will block until shutdown.
     *
     * @return this instance
     */
    ElementsLocal run();

    /**
     * Gets the root {@link ElementRegistry} used by the local runner.
     *
     * @return the {@link ElementRegistry}
     */
    MutableElementRegistry getRootElementRegistry();

    /**
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
