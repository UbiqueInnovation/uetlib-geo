package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.RoutingLinkEdgeProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.Geometry;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class CompleteGraphVertex<C extends Coordinate, P> implements GraphVertex<C, P> {

    private final long id;

    private final  C coordinate;

    private final Collection<CompleteGraphNeighbor<P>> metadata;

    public CompleteGraphVertex(long id, C coordinate, Collection<CompleteGraphNeighbor<P>> metadata) {
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
    public Collection<CompleteGraphNeighbor<P>> metadata() {
        return metadata;
    }

    @Override
    public String layer() {
        return "routing";
    }

    @Override
    public ObjectUtils.Null tags() {
        return null;
    }

    @Override
    public Geometry geometry() {
        return coordinate.toJtsPoint();
    }

    public static <U extends Coordinate, S extends CoordinateVertexProperties<U>, T> GraphVertex<U, T> from(final Vertex<S, RoutingLinkEdgeProperties<T>> vertex) {
        return new CompleteGraphVertex<>(
                vertex.getId(),
                vertex.getProperties().coordinate(),
                vertex.getEdges().stream().map(e -> CompleteGraphNeighbor.fromEdge(vertex.getId(), e)).toList()
        );
    }

    public static <U extends Coordinate, S extends CoordinateVertexProperties<U>, T> Collection<GraphVertex<U, T>> from(final RoutingGraphs<S, T> routingGraphs) {
        return routingGraphs
                .completeGraph()
                .getVertices()
                .stream()
                .map(CompleteGraphVertex::from)
                .toList();
    }
}
