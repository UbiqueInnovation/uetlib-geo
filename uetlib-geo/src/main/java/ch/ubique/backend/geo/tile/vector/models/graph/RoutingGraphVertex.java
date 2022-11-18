package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class RoutingGraphVertex<P> implements GraphVertex<P> {

    private final long id;
    private final Point point;

    private final GraphVertexTags tags;

    private final Collection<RoutingGraphNeighbor<P>> metadata;

    public RoutingGraphVertex(long id, Point point, GraphVertexTags tags, Collection<RoutingGraphNeighbor<P>> metadata) {
        this.id = id;
        this.point = point;
        this.tags = tags;
        this.metadata = metadata;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Point point() {
        return point;
    }

    @Override
    public GraphVertexTags tags() {
        return tags;
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
        return point;
    }

    public static <T> GraphVertex<T> from(final Vertex<Coordinate, RoutingGraphEdgeProperties<T>> vertex, final GeometryFactory geometryFactory) {
        return new RoutingGraphVertex<>(
                vertex.getId(),
                geometryFactory.createPoint(vertex.getProperties()),
                new GraphVertexTags(vertex.getProperties()),
                vertex.getEdges().stream().map(e -> RoutingGraphNeighbor.fromEdge(vertex.getId(), e)).toList()
        );
    }

    public static <T> Collection<GraphVertex<T>> from(final Graph<Coordinate, RoutingGraphEdgeProperties<T>> routingGraph, final GeometryFactory geometryFactory) {
        return routingGraph
                .getVertices()
                .stream()
                .map(v -> from(v, geometryFactory))
                .toList();
    }

}
