package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.EdgeProperties;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.VertexProperties;
import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.Way;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmParser {

    public static Graph parse(InputStream inputStream, OsmParserConfig config) {
        var osmParser = new OsmParser(inputStream, config);
        return osmParser.parse();
    }

    private final InputStream inputStream;
    private final OsmParserConfig config;

    private Graph graph;

    private long nodeCount;
    private long nodeStartMillis;
    private long wayCount;
    private long wayStartMillis;

    private final Map<Long, Long> nodeIdToVertexId;

    private final Logger logger;

    private OsmParser(InputStream inputStream, OsmParserConfig config) {
        this.inputStream = inputStream;
        this.config = config;
        this.nodeIdToVertexId = new HashMap<>();
        logger = LoggerFactory.getLogger(getClass());
    }

    private Graph parse() {
        nodeCount = 0;
        nodeStartMillis = -1;

        wayCount = 0;
        wayStartMillis = -1;

        graph = new Graph();

        var numThreads = 1;
        var parser = new ParallelBinaryParser(inputStream, numThreads);
        parser.onNode(this::processNodes);
        parser.onWay(this::processWays);
        parser.parse();

        return graph;
    }

    private void processNodes(Node node) {

        // Start timer
        if (nodeStartMillis == -1) {
            nodeStartMillis = System.currentTimeMillis();
        }

        // Get or create vertex
        var id = config.useSourceIds() ? node.getId() : nodeCount;
        nodeIdToVertexId.put(node.getId(), id);

        var vertex = graph.getVertex(id);
        nodeCount++;

        // Assign coordinate
        var coordinate = new WGS84Coordinate(node.getLat(), node.getLon());
        vertex.setCoordinate(coordinate);

        // Assign tags
        if (config.parseNodeTags()) {
            var properties = new VertexProperties();
            properties.setTags(node.getTags());
            vertex.setProperties(properties);
        }

        var logInterval = 1000000;
        if (nodeCount % logInterval == 0) {
            double secondsPassed = (System.currentTimeMillis() - nodeStartMillis) / 1000.;
            var count = String.format("%,d", nodeCount);
            var throughput = String.format("%,.0f", logInterval / secondsPassed);
            logger.info("Processed {} nodes at a current rate of {} [nodes/s]", count, throughput);
            nodeStartMillis = System.currentTimeMillis();
        }
    }

    private void processWays(Way way) {

        // Start timer
        if (wayStartMillis == -1) {
            wayStartMillis = System.currentTimeMillis();
        }

        List<Long> nodeIds = way.getNodes();

        for (var i = 0; i < nodeIds.size() - 1; ++i) {
            var u = nodeIdToVertexId.get(nodeIds.get(i));
            var v = nodeIdToVertexId.get(nodeIds.get(i + 1));

            var edge = graph.addEdge(u, v);

            // Assign tags
            if (config.parseWayTags()) {
                var properties = new EdgeProperties();
                properties.setTags(way.getTags());
                edge.setProperties(properties);
            }
        }

        wayCount++;
        var logInterval = 100000;
        if (wayCount % logInterval == 0) {
            double secondsPassed = (System.currentTimeMillis() - wayStartMillis) / 1000.;
            var count = String.format("%,d", wayCount);
            var throughput = String.format("%,.0f", logInterval / secondsPassed);
            logger.info("Processed {} ways at a current rate of {} [ways/s]", count, throughput);
            wayStartMillis = System.currentTimeMillis();
        }
    }
}
