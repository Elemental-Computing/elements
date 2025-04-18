package dev.getelements.elements.sdk.model.exception.schema;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class MetadataSpecNotFoundException extends NotFoundException {
    public MetadataSpecNotFoundException() {}

    public MetadataSpecNotFoundException(String message) {
        super(message);
    }

    public MetadataSpecNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataSpecNotFoundException(Throwable cause) {
        super(cause);
    }

    public MetadataSpecNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
