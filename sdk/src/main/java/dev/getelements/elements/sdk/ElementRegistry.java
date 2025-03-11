package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a registry for {@link Element} instances. Inside an {@link Element}, there is a shared registry available
 * by using the {@link java.util.ServiceLoader} to find an instance of {@link ElementRegistrySupplier}. This provides
 * access to registries in a hierarchy while allowing for visibility into other {@link Element} instances.
 *
 * When searching the hierarchy, by name, the registry searches for all registered at its level, and then up to the
 * parent level and so forth until the root level. The {@link Stream} returned from the {@link #find(String)} method
 * will always return the closest first.
 *
 * Note, instances of {@link ElementRegistry} must be thread-safe and may implement thread safety using a locking
 * strategy.
 */
public interface ElementRegistry extends AutoCloseable {

    /**
     * A name binding for the root {@link ElementRegistry}
     */
    String ROOT = "dev.getelements.elements.sdk.element.registry.root";

    /**
     * Streams all {@link Element} isntances in this {@link ElementRegistry}.
     *
     * @return the {@link Stream} of {@link Element}s
     */
    Stream<Element> stream();

    /**
     * Gets all {@link Element} instances which was registered to this {@link ElementRegistry} specifying the name of
     * the {@link Element}
     *
     * @param name the name of the {@link Element}
     * @return an {@link Optional} of the element
     */
    default Stream<Element> find(final String name) {
        return stream().filter(element -> element.getElementRecord().definition().name().equals(name));
    }

    /**
     * Using the supplied {@link ElementLoader}, registers an {@link Element} to this instance, making it available to
     * all child and sibling {@link Element}s.
     *
     * @param loader the {@link ElementLoader} to supply the {@link Element}
     */
    default Element register(final ElementLoader loader) {

        final var element = loader.load(this);

        try {
            return register(element);
        } catch (Exception ex) {
            element.close();
            throw ex;
        }

    }

    /**
     * Registers an {@link Element} to this instance, making it available to all child and sibling {@link Element}s.
     *
     * @param loader the {@link ElementLoader} to supply the {@link Element}
     */
    Element register(Element loader);

    /**
     * Unregisters an {@link Element}, provided it was previously registered.
     *
     * @return if the Element existed, and was successfully removed.
     */
    boolean unregister(Element element);

    /**
     * Returns a new subordinate registry. This registry will be linked to this registry and inherit all currently
     * registered {@link Element}s contained in. Searching the new subordinate registry will make all registered
     * {@link Element}s available that are availble to this registry.
     *
     * @return a new subordinate
     */
    ElementRegistry newSubordinateRegistry();

    /**
     * Publishes an {@link Event} to the {@link Element} instances within this {@link ElementRegistry}.
     *
     * @param event the event
     */
    void publish(Event event);

    /**
     * Adds a {@link Consumer} to this {@link ElementRegistry} which will get called when an {@link Event} is sourced
     * from an {@link Element} within this registry.
     *
     * @param onEvent the event consumer
     * @return a {@link Subscription} to the event
     */
    Subscription onEvent(Consumer<Event> onEvent);

    /**
     * Adds a {@link Consumer} to this {@link ElementRegistry} which will get called when it is closed.
     * @param onClose the on close consumer
     * @return a {@link Subscription} to the event
     */
    Subscription onClose(Consumer<ElementRegistry> onClose);

    /**
     * Closes all {@link Element} instances within this {@link ElementRegistry} and closes the registry itself.
     */
    void close();

    /**
     * Creates a new instance of the {@link ElementRegistry} using the system default SPI.
     *
     * @return new {@link ElementRegistry}
     */
    static ElementRegistry newDefaultInstance() {
        return ServiceLoader
                .load(ElementRegistry.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No ElementRegistry SPI Available."))
                .get();
    }

}
