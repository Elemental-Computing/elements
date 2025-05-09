package dev.getelements.elements.test;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.Worker;

/**
 * A container for a client {@link Instance}.
 */
public interface EmbeddedWorkerInstanceContainer extends EmbeddedInstanceContainer {

    /**
     * Gets the underlying {@link Worker}.
     *
     * @return the worker
     */
    Worker getWorker();

    /**
     * Gets the worker's bind address.
     *
     * @return the bind address
     */
    String getBindAddress();

    /**
     * Enables the default HTTP client for the {@link Worker} contained in this container.
     * s
     * @return this instance
     */
    EmbeddedWorkerInstanceContainer withDefaultHttpClient();

    /**
     * Returns the default Worker IoC resolver. The definition of the default worker is implementation specific.
     * Typically, this is the fist configured {@link ApplicationId}
     *
     * @return the default worker IoC resolver.
     */
    ServiceLocator getIocResolver();

    /**
     * Gets the {@link ServiceLocator} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param applicationId
     * @return the {@link NodeId}
     */
    ServiceLocator getIocResolver(NodeId applicationId);

    /**
     * Gets the {@link ServiceLocator} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param applicationId the {@link ApplicationId} of the application to fetch
     * @return the {@link NodeId}
     */
    default ServiceLocator getIocResolver(final ApplicationId applicationId) {
        final var nodeId = NodeId.forInstanceAndApplication(getInstanceId(), applicationId);
        return getIocResolver(nodeId);
    }

    /**
     * Fetches the default {@link NodeId} installed in this container. The definition of the default worker is
     * implementation specific. Typically, this is the fist configured {@link ApplicationId}.
     *
     * @return the {@link NodeId}
     */
    default NodeId getNodeId() {
        return getIocResolver().getInstance(NodeId.class);
    }

    /**
     * Fetches the default {@link ApplicationId} installed in this container. The definition of the default worker is
     * implementation specific. Typically, this is the fist configured {@link ApplicationId}.
     *
     * @return the {@link NodeId}
     */
    default ApplicationId getApplicationId() {
        return getNodeId().getApplicationId();
    }

}
