package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphUtil;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParser;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParserConfig;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

        try (final var inputStream =
                new URL("https://download.geofabrik.de/europe/liechtenstein-latest.osm.pbf")
                        .openStream()) {
            final var graph =
                    OsmGraphParser.parse(inputStream, config)
                            .map(
                                    v ->
                                            new Vertex<>(
                                                    v.getId(),
                                                    v.getProperties().coordinate().toJts()),
                                    e -> new Weight(0));

            Assertions.assertTrue(graph.getVertices().size() > 0);
            Assertions.assertTrue(graph.getDirectedEdges().size() > 0);

            final var routingGraph = RoutingGraphUtil.createRoutingGraph(graph);
            Assertions.assertTrue(routingGraph.getVertices().size() > 0);
            Assertions.assertTrue(routingGraph.getDirectedEdges().size() > 0);
        }
    }
}
