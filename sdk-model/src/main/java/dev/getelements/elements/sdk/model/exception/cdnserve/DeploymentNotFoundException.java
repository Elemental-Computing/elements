package dev.getelements.elements.sdk.model.exception.cdnserve;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class DeploymentNotFoundException extends NotFoundException {
    public DeploymentNotFoundException() {}

    public DeploymentNotFoundException(String message) {
        super(message);
    }

    public DeploymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeploymentNotFoundException(Throwable cause) {
        super(cause);
    }

    public DeploymentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
