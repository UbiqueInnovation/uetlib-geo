package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class LinesToRoutingGraphUtil {

    private LinesToRoutingGraphUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(LinesToRoutingGraphUtil.class);

    public static <T> Graph<Coordinate, T> buildGraph(
            final Collection<LineWithInfo<T>> paths) {

        final var graph = new Graph<Coordinate, T>();

        logger.info("Creating vertices");

        final var lastId = new AtomicInteger();
        final var positionToVertex = new HashMap<VertexPosition, Vertex<Coordinate, T>>();
        for (final var path : paths) {
            for (final var coordinate : path.line().getCoordinates()) {
                final var vertexPosition = new VertexPosition(coordinate);
                positionToVertex.computeIfAbsent(vertexPosition, key -> {
                    final var vertexId = lastId.incrementAndGet();
                    final var vertex = new Vertex<Coordinate, T>(vertexId, coordinate);
                    graph.addVertex(vertex);
                    return vertex;
                });
            }
        }

        logger.info("Creating edges by linking neighbors of each path");
        for (final var path : paths) {
            final var points = path.line().getCoordinates();
            for (int i = 0; i < points.length - 1; i++) {
                final var from = positionToVertex.get(new VertexPosition(points[i]));
                final var to = positionToVertex.get(new VertexPosition(points[i + 1]));
                if (from != to) {
                    graph.getOrAddEdge(from, to, path.info());
                }
            }
        }

        return graph;
    }
}
