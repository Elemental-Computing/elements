package dev.getelements.elements.rt.manifest.startup;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class StartupModule {

    @NotNull
    private String module;

    @NotNull
    private Map<String, StartupOperation> operationsByName;

    /**
     * Gets the name fo the module.  This typically names the language-specific type or class name
     * which is used to load the underlying logic.
     *
     * @return the name of the module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the name of the module.
     *
     * @param module the name of the module
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Gets a mapping of operations by name.
     *
     * @return a map of operations by name.
     */
    public Map<String, StartupOperation> getOperationsByName() {
        return operationsByName;
    }

    /**
     * Sets the map of operations by name.
     *
     * @param operationsByName the map of operations by name
     */
    public void setOperationsByName(Map<String, StartupOperation> operationsByName) {
        this.operationsByName = operationsByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartupModule)) return false;

        StartupModule that = (StartupModule) o;

        if (getModule() != null ? !getModule().equals(that.getModule()) : that.getModule() != null) return false;
        return getOperationsByName() != null ? getOperationsByName().equals(that.getOperationsByName()) : that.getOperationsByName() == null;
    }

    @Override
    public int hashCode() {
        int result = getModule() != null ? getModule().hashCode() : 0;
        result = 31 * result + (getOperationsByName() != null ? getOperationsByName().hashCode() : 0);
        return result;
    }

}
