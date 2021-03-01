package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.Way;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmParser {

    public static Graph parse(InputStream inputStream) {
        OsmParser osmParser = new OsmParser(inputStream);
        return osmParser.parse();
    }

    private final InputStream inputStream;
    private Graph graph;

    private int nodeCount;
    private int wayCount;

    private final Logger logger;

    private OsmParser(InputStream inputStream) {
        this.inputStream = inputStream;
        logger = LoggerFactory.getLogger(getClass());
    }

    private Graph parse() {
        graph = new Graph();

        int numThreads = 1;
        ParallelBinaryParser parser = new ParallelBinaryParser(inputStream, numThreads);
        parser.onNode(this::processNodes);
        parser.onWay(this::processWays);
        parser.parse();

        return graph;
    }

    private void processNodes(Node node) {
        // Get or create vertex
        Vertex vertex = graph.getVertex(node.getId());

        // Assign coordinate
        var coordinate = new WGS84Coordinate(node.getLat(), node.getLon());
        vertex.setCoordinate(coordinate);

        nodeCount++;
        if (nodeCount % 1000000 == 0) {
            logger.info("Proccesed {} nodes", nodeCount);
        }
    }

    private void processWays(Way way) {
        List<Long> nodeIds = way.getNodes();

        for (int i = 0; i < nodeIds.size() - 1; ++i) {
            var u = nodeIds.get(i);
            var v = nodeIds.get(i + 1);

            graph.addEdge(u, v);
        }

        wayCount++;
        if (wayCount % 1000000 == 0) {
            logger.info("Proccesed {} ways", wayCount);
        }
    }
}
