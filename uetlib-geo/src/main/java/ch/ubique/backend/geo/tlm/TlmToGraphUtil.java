package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.TlmVertexPosition;
import ch.ubique.backend.geo.tlm.models.TlmVertexProperties;
import ch.ubique.backend.geo.tlm.models.WegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;


public class TlmToGraphUtil {

    private TlmToGraphUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(TlmToGraphUtil.class);

    private static final Set<WegType> forbiddenWays = Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    public static Graph<TlmVertexProperties, TlmPath> buildGraph(Collection<TlmPath> paths) {

        final var graph = new Graph<TlmVertexProperties, TlmPath>();

        final var relevantPaths = paths.stream()
                .filter(path -> !forbiddenWays.contains(path.getType()))
                .toList();

        logger.info("Creating vertices");

        var lastId = 0;
        final var positionToVertex = new HashMap<TlmVertexPosition, Vertex<TlmVertexProperties, TlmPath>>();
        for (final var path : relevantPaths) {
            for (final var coordinate : path.getPolyline()) {
                final var vertexPosition = new TlmVertexPosition(coordinate);
                if (!positionToVertex.containsKey(vertexPosition)) {
                    final var vertexId = ++lastId;
                    positionToVertex.put(vertexPosition, new Vertex<>(vertexId, new TlmVertexProperties(coordinate)));
                    graph.addVertex(new Vertex<>(vertexId, new TlmVertexProperties(coordinate)));
                }
            }
        }

        logger.info("Creating edges by linking neighbors of each path");
        for (final var path : relevantPaths) {
            final var points = path.getPolyline();
            for (int i = 0; i < points.size() - 1; i++) {
                final var from = positionToVertex.get(new TlmVertexPosition(points.get(i)));
                final var to = positionToVertex.get(new TlmVertexPosition(points.get(i + 1)));
                if (from != to) {
                    graph.getOrAddEdge(from, to, path);
                }
            }
        }

        return graph;
    }
}
