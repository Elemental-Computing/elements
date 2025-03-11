package dev.getelements.elements.app.serve;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementDefinition;

/**
 * Constants for the Application Server (often times called appserve) component.
 */
public interface AppServeConstants {

    /**
     * Defines an attribute which specifies the prefix for the element. At laod-time, loader will inspect the
     * {@link Attributes} from the {@link Element}. If blank ({@see {@link String#isBlank()}}, then the loader will
     * defer to the value of {@link ElementDefinition#value()}, which would typically be the name of the package
     * bearing the annotation.
     */
    @ElementDefaultAttribute("")
    String APPLICATION_PREFIX = "dev.getelements.elements.app.serve.prefix";

}
