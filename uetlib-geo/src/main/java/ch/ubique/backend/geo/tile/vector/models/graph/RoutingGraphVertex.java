package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import org.locationtech.jts.geom.Geometry;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class RoutingGraphVertex<C extends Coordinate, P> implements GraphVertex<C, P> {

    private final long id;
    private final C coordinate;

    private final Collection<RoutingGraphNeighbor<P>> metadata;

    public RoutingGraphVertex(long id, C coordinate, Collection<RoutingGraphNeighbor<P>> metadata) {
        this.id = id;
        this.coordinate = coordinate;
        this.metadata = metadata;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public C coordinate() {
        return coordinate;
    }

    @Override
    public Collection<RoutingGraphNeighbor<P>> metadata() {
        return metadata;
    }

    @Override
    public String layer() {
        return "routing";
    }

    @Override
    public Geometry geometry() {
        return coordinate.toJtsPoint();
    }

    public static <U extends Coordinate, S extends CoordinateVertexProperties<U>, T> GraphVertex<U, T> from(final Vertex<S, RoutingGraphEdgeProperties<S, T>> vertex) {
        return new RoutingGraphVertex<>(
                vertex.getId(),
                vertex.getProperties().coordinate(),
                vertex.getEdges().stream().map(e -> RoutingGraphNeighbor.fromEdge(vertex.getId(), e)).toList()
        );
    }

    public static <U extends Coordinate, S extends CoordinateVertexProperties<U>, T> Collection<GraphVertex<U, T>> from(final RoutingGraphs<S, T> routingGraphs) {
        return routingGraphs
                .routingGraph()
                .getVertices()
                .stream()
                .map(RoutingGraphVertex::from)
                .toList();
    }
}
