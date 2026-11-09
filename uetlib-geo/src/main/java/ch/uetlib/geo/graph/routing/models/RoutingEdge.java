package ch.uetlib.geo.graph.routing.models;

public sealed interface RoutingEdge permits RoutingCompleteEdge, RoutingShortcutEdge {
    float weight();
}
