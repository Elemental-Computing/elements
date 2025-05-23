package dev.getelements.elements.sdk.model.exception.crypto;

import dev.getelements.elements.sdk.model.exception.InternalException;

public class CryptoException extends InternalException {

    public CryptoException() {}

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

    public CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
