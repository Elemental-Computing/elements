package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.largeobject.LargeObjectContentNotFoundException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a bucket for storing the contents of {@link LargeObject}s.
 */
@ElementServiceExport
public interface LargeObjectBucket {

    /**
     * Writes the {@link LargeObject}'s contents
     *
     * @param objectId the object id
     * @return the {@link InputStream} which can be used to read the {@link LargeObject} contents
     * @throws IOException if an error occurs opening the contents
     */
    OutputStream writeObject(String objectId) throws IOException;

    /**
     * Reads the {@link LargeObject}'s contents
     *
     * @param objectId the object id
     * @return the {@link InputStream} which can be used to read the {@link LargeObject} contents
     * @throws IOException                         if an error occurs opening the contents
     * @throws LargeObjectContentNotFoundException if the content does not exist for the object.
     */
    InputStream readObject(String objectId) throws IOException;

    /**
     * Deletes the {@link LargeObject} with the supplied id as well as the associated metadata.
     *
     * @param objectId the object id
     */
    void deleteLargeObject(String objectId) throws IOException;

    /**
     * Forces buckets to mark referenced LO to set state UPLOADED
     *
     * @param largeObject the large object
     */
    void setUploaded(LargeObject largeObject);

}
