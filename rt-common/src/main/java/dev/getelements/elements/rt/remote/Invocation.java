package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.annotation.Dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely.
 */
public class Invocation implements Serializable {

    private String type;

    private String name;

    private String method;

    private List<String> parameters;

    private List<Object> arguments;

    private Dispatch.Type dispatchType;

    /**
     * Gets the string representing the type of the remote object to invoke.  {@see {@link Class#getName()}}
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the string representing the type of the remote object to invoke.  {@see {@link Class#getName()}}
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the name of the remote object to invoke.  {@see {@link jakarta.inject.Named}}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the remote object to invoke.  {@see {@link jakarta.inject.Named}}.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the remote method to invoke.  {@see {@link Method#getName()}}.
     *
     * @return the name
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the name of the remote method to invoke.  {@see {@link Method#getName()}}.
     *
     * @param method the method name
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets a listing of the names of the method parameters.  Each parameter is named for the {@link Class<?>} it
     * represents.
     *
     * @return the {@link List<String>} of parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Sets a listing of the names of the method parameters.  Each parameter is named for the {@link Class<?>} it
     * represents.
     *
     * @param parameters the {@link List<String>} of parameters
     */
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @return a {@link List<Object>} containing the arguments to pass to the remote method
     */
    public List<Object> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @param arguments {@link List<Object>} containing the arguments to pass to the remote method
     */
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    /**
     * Indicates the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the invocation
     * can be routed.
     *
     * @return the {@link Dispatch.Type}
     */
    public Dispatch.Type getDispatchType() {
        return dispatchType;
    }

    /**
     * Sets the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the invocation can be
     * routed.
     */
    public void setDispatchType(Dispatch.Type dispatchType) {
        this.dispatchType = dispatchType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invocation that = (Invocation) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getMethod(), that.getMethod()) &&
                Objects.equals(getParameters(), that.getParameters()) &&
                Objects.equals(getArguments(), that.getArguments()) &&
                getDispatchType() == that.getDispatchType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName(), getMethod(), getParameters(), getArguments(), getDispatchType());
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", method='" + method + '\'' +
                ", parameters=" + parameters +
                ", arguments=" + arguments +
                ", dispatchType=" + dispatchType +
                '}';
    }
}
