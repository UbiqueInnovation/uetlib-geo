package ch.ubique.backend.geo.graph.io.geopackage;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;

import org.apache.commons.lang3.NotImplementedException;
import org.geotools.geopkg.GeoPackage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RoutingGraphGeopackageEncoder<C extends Coordinate> implements AutoCloseable {

    private final CoordinateSystem<C> coordinateSystem;
    private final GraphGeopackageEncoder<RoutingVertex, C> graphGeopackageEncoder;

    public RoutingGraphGeopackageEncoder(final CoordinateSystem<C> coordinateSystem)
            throws IOException {
        this.coordinateSystem = coordinateSystem;
        this.graphGeopackageEncoder =
                new GraphGeopackageEncoder<>(this::getCoordinate, coordinateSystem);
    }

    public void encode(final RoutingGraphs graphs) throws IOException {
        graphGeopackageEncoder
                .layerPrefix("complete_")
                .encode(graphs.completeGraph().map(Vertex::getProperties, this::edgeProperties));
        graphGeopackageEncoder
                .layerPrefix("routing_")
                .encode(graphs.routingGraph().map(Vertex::getProperties, this::edgeProperties));
    }

    public GeoPackage geoPackage() {
        return graphGeopackageEncoder.geoPackage();
    }

    private C getCoordinate(final RoutingVertex vertexProperties) {
        return coordinateSystem.fromJts(vertexProperties.coordinate());
    }

    @Override
    public void close() {
        graphGeopackageEncoder.close();
    }

    private Map<String, Object> edgeProperties(final Edge<RoutingVertex, ?> edge) {
        final var map = new HashMap<>(weightProperties(edge));
        if (edge.getProperties() instanceof final RoutingShortcutEdge routingShortcutEdge) {
            map.put("direction", routingShortcutEdge.direction().getId());
        }
        return map;
    }

    private Map<String, Object> weightProperties(final Edge<RoutingVertex, ?> edge) {
        final var properties = edge.getProperties();
        final var weight = weight(properties);
        final var length = length(edge);
        return Map.ofEntries(
                Map.entry("weight", weight),
                Map.entry("length", length),
                Map.entry("speed", length / weight),
                Map.entry("scaled_weight", weight / length));
    }

    private static float weight(final Object properties) {
        if (properties instanceof final RoutingEdge routingEdge) {
            return routingEdge.weight();
        }
        throw new NotImplementedException();
    }

    private static float length(final Edge<RoutingVertex, ?> edge) {
        return (float)
                edge.getFrom()
                        .getProperties()
                        .coordinate()
                        .distance(edge.getTo().getProperties().coordinate());
    }
}
