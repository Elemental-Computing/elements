package dev.getelements.elements.sdk.model.exception.mission;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ProgressNotFoundException extends NotFoundException {

    public ProgressNotFoundException() {}

    public ProgressNotFoundException(String message) {
        super(message);
    }

    public ProgressNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProgressNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProgressNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
