package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.ModuleNotFoundException;
import dev.getelements.elements.rt.exception.ResourcePersistenceException;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.cluster.path.Path;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;

/**
 * Loads instances of {@link Resource} based on {@link Path} instances.  Typically instances of this work in conjunction
 * with an AssetLoader to load the raw bytes from storage and then into memory.
 */
public interface ResourceLoader extends AutoCloseable {

    /**
     * Loads the {@link Resource} from the {@link InputStream} in non-verbose mode.
     *
     * @param is the {@link InputStream} from which to load the {@link Resource}
     * @return the {@link Resource}
     * @throws ResourcePersistenceException
     */
    default Resource load(final InputStream is) throws ResourcePersistenceException {
        return load(is, false);
    }

    /**
     * Loads the {@link Resource} from the {@link ReadableByteChannel} in non-verbose mode.
     *
     * @param rbc the {@link InputStream} from which to load the {@link Resource}
     * @return the {@link Resource}
     * @throws ResourcePersistenceException
     */
    default Resource load(final ReadableByteChannel rbc) throws ResourcePersistenceException {
        return load(rbc, false);
    }

    /**
     * Loads a {@link Resource} from the supplied {@link InputStream}.  The contents of the {@link InputStream} should
     * be generated form a call to {@link Resource#serialize(OutputStream)}.
     *
     * {@see {@link Resource#setVerbose(boolean)}}
     *
     * @param is the {@link InputStream} from which to load the {@link Resource}
     * @param verbose indicates if the resource shoudl be loaded verbosely.
     * @return the loaded {@link Resource}
     *
     * @throws ResourcePersistenceException if the resource was corrupted of failed to load for some reason
     *
     */
    Resource load(final InputStream is, boolean verbose) throws ResourcePersistenceException;

    /**
     * Loads a {@link Resource} from the supplied {@link ReadableByteChannel}.  The contents of the {@link InputStream}
     * should be generated form a call to {@link Resource#serialize(OutputStream)} or
     * {@link Resource#serialize(WritableByteChannel)}.
     *
     * {@see {@link Resource#setVerbose(boolean)}}
     *
     * @param rbc the {@link InputStream} from which to load the {@link Resource}
     * @param verbose indicates if the resource shoudl be loaded verbosely.
     * @return the loaded {@link Resource}
     *
     * @throws ResourcePersistenceException if the resource was corrupted of failed to load for some reason
     *
     */
    Resource load(final ReadableByteChannel rbc, boolean verbose) throws ResourcePersistenceException;

    /**
     * Performs the same operation as {@link #load(String, Attributes, Object...)} using {@link Attributes#EMPTY}
     * as the specified {@link Attributes}.
     */
    default Resource load(final String modulePath, final Object ... args) throws ModuleNotFoundException {
        return load(modulePath, emptyAttributes(), args);
    }

    /**
     * Loads the {@link Resource} specified by the supplied module name.  The supplied module name is specific to
     * the particular implementation of the {@link ResourceLoader}, but should be a unique identifier specifying the
     * unit or module of code to load.
     *
     * Since a {@link Resource} can be represented by any number of languages, the string passed is highly specific
     * to the underlying implementation's semantics.
     *
     * @param moduleName the module name
     * @param attributes {@link Attributes} assocaited with the {@link Resource}
     * @param args various initialization arguments to be passed to the underlying {@link Resource}
     *
     * @return the {@link Resource} instance, never null
     *
     * @throws ModuleNotFoundException if the source for the {@link Resource} cannot be found.
     */
    Resource load(String moduleName, Attributes attributes, Object ... args) throws ModuleNotFoundException;

    /**
     * Closes the {@link ResourceLoader} and cleaning up any resources.  Any open {@link Resource}
     * instances may be closed, but this is not a guarantee.  All resources open <b>should</b> be closed
     * before closing this {@link ResourceLoader}.  Using {@link Resource}s after closing this instance, or
     * closing this instance while resources are open should be considered undefined behavior.
     *
     * This also closes the resources which may be associated with this {@link ResourceLoader}.
     *
     * Invoking this method twice on the same object should also be considered undefined behavior.
     */
    @Override
    void close();

}
