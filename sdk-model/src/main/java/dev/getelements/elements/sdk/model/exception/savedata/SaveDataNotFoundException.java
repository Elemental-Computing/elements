package dev.getelements.elements.sdk.model.exception.savedata;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;

/**
 * Thrown when an instance of {@link SaveDataDocument} can't be found.
 */
public class SaveDataNotFoundException extends NotFoundException {

    public SaveDataNotFoundException() {}

    public SaveDataNotFoundException(String message) {
        super(message);
    }

    public SaveDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SaveDataNotFoundException(Throwable cause) {
        super(cause);
    }

    public SaveDataNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
