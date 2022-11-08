package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.graph.routing.RoutingGraphUtil;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParser;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParserConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

class OsmGraphParserTest {

    @Test
    @Disabled("Depends on local file")
    void testParsingOSM() throws IOException {
        final var config =
                OsmGraphParserConfig.builder()
                        .parseNodeTags(true)
                        .parseWayTags(true)
                        .useSourceIds(true)
                        .build();

        try (final var inputStream = new URL("https://download.geofabrik.de/europe/liechtenstein-latest.osm.pbf").openStream()){
            final var graph = OsmGraphParser.parse(inputStream, config);
            Assertions.assertTrue(graph.getVertices().size() > 0);
            Assertions.assertTrue(graph.getEdges().size() > 0);

            final var routingGraphs = RoutingGraphUtil.createRoutingGraph(graph);
            Assertions.assertTrue(routingGraphs.routingGraph().getVertices().size() > 0);
            Assertions.assertTrue(routingGraphs.routingGraph().getEdges().size() > 0);



            final var allVerticesHaveNonNegativeId =
                    routingGraphs.completeGraph().getVertices().stream().allMatch(v -> v.getId() >= 0);
            Assertions.assertTrue(allVerticesHaveNonNegativeId);

            final var edgesWithoutRoutingId =
                    routingGraphs.completeGraph().getEdges().stream()
                            .filter(e -> e.getProperties().getRoutingEdgeId() == null)
                            .count();
            Assertions.assertEquals(0, edgesWithoutRoutingId, "Expected all edges to have a routing id.");
        }

    }
}
