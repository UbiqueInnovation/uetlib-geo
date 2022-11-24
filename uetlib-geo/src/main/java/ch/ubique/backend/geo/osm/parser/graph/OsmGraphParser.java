package ch.ubique.backend.geo.osm.parser.graph;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.osm.parser.osmosis.OsmParser;
import ch.ubique.backend.geo.osm.parser.osmosis.OsmUtils;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class OsmGraphParser {

    public static Graph<OsmVertexProperties, OsmEdgeProperties> parse(InputStream inputStream, OsmGraphParserConfig config) {
        var osmParser = new OsmGraphParser(inputStream, config);
        return osmParser.parse();
    }

    private final InputStream inputStream;
    private final OsmGraphParserConfig config;

    private final Graph<OsmVertexProperties, OsmEdgeProperties> graph;

    private long nodeCount;
    private long nodeStartMillis;
    private long wayCount;
    private long wayStartMillis;

    private final Map<Long, Long> nodeIdToVertexId;

    private static final Logger logger = LoggerFactory.getLogger(OsmGraphParser.class);

    private OsmGraphParser(InputStream inputStream, OsmGraphParserConfig config) {
        this.inputStream = inputStream;
        this.config = config;
        this.nodeIdToVertexId = new HashMap<>();

        nodeCount = 0;
        nodeStartMillis = -1;

        wayCount = 0;
        wayStartMillis = -1;

        graph = new Graph<>();
    }

    private Graph<OsmVertexProperties, OsmEdgeProperties> parse() {
        OsmParser
                .from(inputStream)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .parse();

        return graph;
    }

    private void processNodes(Node node) {
        // Get or create vertex
        var id = config.useSourceIds() ? node.getId() : nodeCount;
        nodeIdToVertexId.put(node.getId(), id);


        // Get coordinate
        var coordinate = OsmUtils.getCoordinate(node);

        // Get tags
        final var tags = new HashMap<String, String>();
        if (config.parseNodeTags()) {
            tags.putAll(OsmUtils.getTags(node));
        }

        final var vertex = graph.getOrAddVertex(id);
        final var properties = new OsmVertexProperties(coordinate, tags);
        vertex.setProperties(properties);

        logNodeProgress();
    }

    private void processWays(Way way) {
        final var nodeIds = way.getWayNodes().stream().map(WayNode::getNodeId).toList();

        // Get tags tags
        final var tags = new HashMap<String, String>();
        if (config.parseWayTags()) {
            tags.putAll(OsmUtils.getTags(way));
        }

        var properties = new OsmEdgeProperties(tags);

        for (var i = 0; i < nodeIds.size() - 1; ++i) {
            var u = nodeIdToVertexId.get(nodeIds.get(i));
            var v = nodeIdToVertexId.get(nodeIds.get(i + 1));

            final var existingEdge = graph.getEdge(u, v);
            if (existingEdge.isPresent()) {
                existingEdge.get().getProperties().tags().putAll(tags); // Merge tags
            } else {
                graph.getOrAddUndirectedEdge(u, v, properties);
            }
        }
        logWayProgress();
    }

    private void logNodeProgress() {
        if (nodeStartMillis == -1) {
            nodeStartMillis = System.currentTimeMillis();
        }
        nodeCount++;
        if (nodeCount % 1000000 == 0) {
            logProgress(nodeCount, 1000000, nodeStartMillis, "Processed {} nodes at a current rate of {} [nodes/s]");
            nodeStartMillis = System.currentTimeMillis();
        }
    }

    private void logWayProgress() {
        if (wayStartMillis == -1) {
            wayStartMillis = System.currentTimeMillis();
        }
        wayCount++;
        if (wayCount % 100000 == 0) {
            logProgress(wayCount, 100000, wayStartMillis, "Processed {} ways at a current rate of {} [ways/s]");
            wayStartMillis = System.currentTimeMillis();
        }
    }

    private void logProgress(long elementCount, int logInterval, long startMillis, String logFormat) {
        double secondsPassed = (System.currentTimeMillis() - startMillis) / 1000.;
        var count = String.format("%,d", elementCount);
        var throughput = String.format("%,.0f", logInterval / secondsPassed);
        logger.info(logFormat, count, throughput);
    }
}
