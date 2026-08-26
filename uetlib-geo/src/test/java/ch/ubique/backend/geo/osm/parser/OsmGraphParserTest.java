package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphUtil;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParser;
import ch.ubique.backend.geo.osm.parser.graph.OsmGraphParserConfig;

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
                URI.create("https://geofabrik-cache.ubique.ch/europe/liechtenstein-latest.osm.pbf")
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
