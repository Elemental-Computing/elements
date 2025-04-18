package dev.getelements.elements.rt.remote;

import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;

import java.util.List;

public interface RoutingStatus {

    InstanceId getInstanceId();

    List<Route> getRoutingTable();

    List<Route> getMasterNodeRoutingTable();

    List<Route> getApplicationNodeRoutingTable();

    interface Route {

        boolean isLocal();

        NodeId getNodeId();

        String getPhysicalDestination();

    }

}
