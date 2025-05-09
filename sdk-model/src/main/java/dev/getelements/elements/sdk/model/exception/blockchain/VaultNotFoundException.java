package dev.getelements.elements.sdk.model.exception.blockchain;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class VaultNotFoundException extends NotFoundException {

    public VaultNotFoundException() {}

    public VaultNotFoundException(String message) {
        super(message);
    }

    public VaultNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VaultNotFoundException(Throwable cause) {
        super(cause);
    }

    public VaultNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
