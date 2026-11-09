package ch.uetlib.geo.osm.parser;

import ch.uetlib.geo.graph.routing.models.RoutingCompleteEdge;
import ch.uetlib.geo.graph.routing.models.RoutingVertex;
import ch.uetlib.geo.graph.routing.utils.RoutingGraphUtil;
import ch.uetlib.geo.osm.parser.graph.OsmGraphParser;
import ch.uetlib.geo.osm.parser.graph.OsmGraphParserConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

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
                URI.create("https://osm-fixture.example.invalid/europe/liechtenstein-latest.osm.pbf")
                        .toURL()
                        .openStream()) {
            final var graph =
                    OsmGraphParser.parse(inputStream, config)
                            .map(
                                    v ->
                                            new RoutingVertex(
                                                    v.getProperties().coordinate().toJts(), false),
                                    e -> new RoutingCompleteEdge(0, null));

            Assertions.assertFalse(graph.getVertices().isEmpty());
            Assertions.assertFalse(graph.getDirectedEdges().isEmpty());

            final var routingGraph = RoutingGraphUtil.createRoutingGraph(graph);
            Assertions.assertFalse(routingGraph.getVertices().isEmpty());
            Assertions.assertFalse(routingGraph.getDirectedEdges().isEmpty());
        }
    }
}
