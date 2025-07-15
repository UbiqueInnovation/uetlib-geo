package ch.ubique.backend.geo.graph.routing.models;

public sealed interface RoutingEdge permits RoutingCompleteEdge, RoutingShortcutEdge {
    float weight();
}
