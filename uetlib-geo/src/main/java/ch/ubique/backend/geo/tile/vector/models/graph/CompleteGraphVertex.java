package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class CompleteGraphVertex<P> implements GraphVertex<P> {

    private final long id;

    private final Point point;

    private final Collection<CompleteGraphNeighbor<P>> metadata;

    public CompleteGraphVertex(long id, Point point, Collection<CompleteGraphNeighbor<P>> metadata) {
        this.id = id;
        this.point = point;
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
    public Collection<CompleteGraphNeighbor<P>> metadata() {
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

    public static <T> GraphVertex<T> from(final Vertex<Coordinate, T> vertex, final GeometryFactory geometryFactory) {
        return new CompleteGraphVertex<>(
                vertex.getId(),
                geometryFactory.createPoint(vertex.getProperties()),
                vertex.getEdges().stream().map(e -> CompleteGraphNeighbor.fromEdge(vertex.getId(), e)).toList()
        );
    }

    public static <T> Collection<GraphVertex<T>> from(final Graph<Coordinate, T> routingGraph, final GeometryFactory geometryFactory) {
        return routingGraph
                .getVertices()
                .stream()
                .map(v -> from(v, geometryFactory))
                .toList();
    }
}
