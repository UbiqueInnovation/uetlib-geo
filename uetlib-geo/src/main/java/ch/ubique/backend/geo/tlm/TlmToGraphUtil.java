package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmInfo;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.TlmVertexPosition;
import ch.ubique.backend.geo.tlm.models.TlmVertexProperties;
import ch.ubique.backend.geo.tlm.models.WegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class TlmToGraphUtil {

    private TlmToGraphUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(TlmToGraphUtil.class);

    private static final Set<WegType> forbiddenWays = Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    public static Graph<TlmVertexProperties, TlmInfo> buildGraph(Collection<TlmPath> paths) {

        final var graph = new Graph<TlmVertexProperties, TlmInfo>();

        final var relevantPaths = paths.stream()
                .filter(path -> !forbiddenWays.contains(path.getType()))
                .toList();

        logger.info("Creating vertices");

        final var lastId = new AtomicInteger();
        final var positionToVertex = new HashMap<TlmVertexPosition, Vertex<TlmVertexProperties, TlmInfo>>();
        for (final var path : relevantPaths) {
            for (final var coordinate : path.getPolyline()) {
                final var vertexPosition = new TlmVertexPosition(coordinate);
                positionToVertex.computeIfAbsent(vertexPosition, key -> {
                    final var vertexId = lastId.incrementAndGet();
                    final var vertex = new Vertex<TlmVertexProperties, TlmInfo>(vertexId, new TlmVertexProperties(coordinate));
                    graph.addVertex(vertex);
                    return vertex;
                });
            }
        }

        logger.info("Creating edges by linking neighbors of each path");
        for (final var path : relevantPaths) {
            final var pathInfo = new TlmInfo(path);
            final var points = path.getPolyline();
            for (int i = 0; i < points.size() - 1; i++) {
                final var from = positionToVertex.get(new TlmVertexPosition(points.get(i)));
                final var to = positionToVertex.get(new TlmVertexPosition(points.get(i + 1)));
                if (from != to) {
                    graph.getOrAddEdge(from, to, pathInfo);
                }
            }
        }

        return graph;
    }
}
