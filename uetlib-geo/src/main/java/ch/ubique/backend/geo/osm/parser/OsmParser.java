package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.EdgeProperties;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.VertexProperties;
import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.Way;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmParser {

    public static Graph parseWithTags(InputStream inputStream) {
        return parse(inputStream, true, true);
    }

    public static Graph parseWithoutTags(InputStream inputStream) {
        return parse(inputStream, false, false);
    }

    public static Graph parse(InputStream inputStream, boolean parseNodeTags, boolean parseWayTags) {
        OsmParser osmParser = new OsmParser(inputStream, parseNodeTags, parseWayTags);
        return osmParser.parse();
    }

    private final InputStream inputStream;
    private final boolean parseNodeTags;
    private final boolean parseWayTags;

    private Graph graph;

    private int nodeCount;
    private long nodeStartMillis;
    private int wayCount;
    private long wayStartMillis;

    private final Logger logger;

    private OsmParser(InputStream inputStream, boolean parseNodeTags, boolean parseWayTags) {
        this.inputStream = inputStream;
        this.parseNodeTags = parseNodeTags;
        this.parseWayTags = parseWayTags;
        logger = LoggerFactory.getLogger(getClass());
    }

    private Graph parse() {
        nodeCount = 0;
        nodeStartMillis = -1;

        wayCount = 0;
        wayStartMillis = -1;

        graph = new Graph();

        int numThreads = 1;
        ParallelBinaryParser parser = new ParallelBinaryParser(inputStream, numThreads);
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
        Vertex vertex = graph.getVertex(node.getId());

        // Assign coordinate
        var coordinate = new WGS84Coordinate(node.getLat(), node.getLon());
        vertex.setCoordinate(coordinate);

        // Assign tags
        if (parseNodeTags) {
            var properties = new VertexProperties();
            properties.setTags(node.getTags());
            vertex.setProperties(properties);
        }

        nodeCount++;
        int logInterval = 1000000;
        if (nodeCount % logInterval == 0) {
            double secondsPassed = (System.currentTimeMillis() - nodeStartMillis) / 1000.;
            String count = String.format("%,d", nodeCount);
            String throughput = String.format("%,.0f", logInterval / secondsPassed);
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

        for (int i = 0; i < nodeIds.size() - 1; ++i) {
            var u = nodeIds.get(i);
            var v = nodeIds.get(i + 1);

            var edge = graph.addEdge(u, v);

            // Assign tags
            if (parseWayTags) {
                var properties = new EdgeProperties();
                properties.setTags(way.getTags());
                edge.setProperties(properties);
            }
        }

        wayCount++;
        int logInterval = 100000;
        if (wayCount % logInterval == 0) {
            double secondsPassed = (System.currentTimeMillis() - wayStartMillis) / 1000.;
            String count = String.format("%,d", wayCount);
            String throughput = String.format("%,.0f", logInterval / secondsPassed);
            logger.info("Processed {} ways at a current rate of {} [ways/s]", count, throughput);
            wayStartMillis = System.currentTimeMillis();
        }
    }
}
