package ch.ubique.backend.geo.osm.parser;

import ch.ubique.backend.geo.graph.utils.RoutingGraphUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

class OsmParserTest {

    @Test
    @Disabled("Depends on local file")
    void testParsingOSM() throws FileNotFoundException {
        final var file = new File("/home/daniel/Downloads/all.pbf");
        final var config =
                OsmParserConfig.builder()
                        .parseNodeTags(true)
                        .parseWayTags(false)
                        .useSourceIds(false)
                        .build();
        final var graph = OsmParser.parse(new FileInputStream(file), config);
        Assertions.assertTrue(graph.getVertices().size() > 0);
        Assertions.assertTrue(graph.getEdges().size() > 0);

        final var routingGraph = RoutingGraphUtil.createRoutingGraph(graph);
        Assertions.assertTrue(routingGraph.getVertices().size() > 0);
        Assertions.assertTrue(routingGraph.getEdges().size() > 0);

        boolean allEdgesHaveRoutingId =
                graph.getEdges().stream()
                        .allMatch(
                                e ->
                                        e.getProperties() != null
                                                && e.getProperties().getRoutingEdgeId() != null);

        boolean allVerticesHaveNonNegativeId =
                graph.getVertices().stream().allMatch(v -> v.getId() >= 0);

        Assertions.assertTrue(allEdgesHaveRoutingId);
        Assertions.assertTrue(allVerticesHaveNonNegativeId);
    }
}
