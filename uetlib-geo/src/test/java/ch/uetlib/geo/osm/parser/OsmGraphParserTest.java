package ch.uetlib.geo.osm.parser;

import static org.assertj.core.api.Assertions.assertThat;

import ch.uetlib.geo.graph.routing.models.RoutingCompleteEdge;
import ch.uetlib.geo.graph.routing.models.RoutingVertex;
import ch.uetlib.geo.graph.routing.utils.RoutingGraphUtil;
import ch.uetlib.geo.osm.parser.graph.OsmGraphParser;
import ch.uetlib.geo.osm.parser.graph.OsmGraphParserConfig;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class OsmGraphParserTest {

    @Test
    void parsesSyntheticRoadsAndBuildsRoutingGraph() throws IOException {
        final var config = OsmGraphParserConfig.builder().build();
        try (final var input = getClass().getResourceAsStream("/osm/synthetic-roads.osm.pbf")) {
            assertThat(input).isNotNull();
            final var graph = OsmGraphParser.parse(input, config);

            assertThat(graph.getVertices()).hasSize(7);
            assertThat(graph.getDirectedEdges()).hasSize(10);
            final var crossing = graph.getVertex(1).orElseThrow().getProperties();
            assertThat(crossing.coordinate().latitude()).isEqualTo(47.0);
            assertThat(crossing.coordinate().longitude()).isEqualTo(8.0);
            assertThat(crossing.tags()).containsEntry("highway", "crossing");
            assertThat(graph.getVertex(2).orElseThrow().degree()).isEqualTo(3);
            assertThat(graph.getEdge(1, 7).orElseThrow().getProperties().tags())
                    .containsEntry("name", "Synthetic Main Street")
                    .containsEntry("maxspeed", "30");
            assertThat(graph.getEdge(2, 4).orElseThrow().getProperties().tags())
                    .containsEntry("oneway", "yes");
            // The raw OSM graph preserves tags but represents every segment as undirected.
            assertThat(graph.getEdge(4, 2)).isPresent();
            assertThat(graph.getEdge(5, 6)).isPresent();
            assertThat(graph.getEdge(2, 5)).isEmpty();

            final var routingGraph =
                    RoutingGraphUtil.createRoutingGraph(
                            graph.map(
                                    v ->
                                            new RoutingVertex(
                                                    v.getProperties().coordinate().toJts(), false),
                                    e -> new RoutingCompleteEdge(0, null)));
            assertThat(routingGraph.getVertices()).hasSize(6);
            assertThat(routingGraph.getDirectedEdges()).hasSize(8);
            assertThat(routingGraph.getVertex(7)).isEmpty();
            assertThat(routingGraph.getEdge(1, 2)).isPresent();
            assertThat(routingGraph.getEdge(5, 6)).isPresent();
            assertThat(routingGraph.getEdge(2, 5)).isEmpty();
        }
    }

    @Test
    void canDiscardTagsAndAssignSequentialVertexIds() throws IOException {
        final var config =
                OsmGraphParserConfig.builder()
                        .parseNodeTags(false)
                        .parseWayTags(false)
                        .useSourceIds(false)
                        .build();
        try (final var input = getClass().getResourceAsStream("/osm/synthetic-roads.osm.pbf")) {
            assertThat(input).isNotNull();
            final var graph = OsmGraphParser.parse(input, config);
            assertThat(graph.getVertices()).hasSize(7);
            assertThat(graph.getDirectedEdges()).hasSize(10);
            assertThat(graph.getVertex(0)).isPresent();
            assertThat(graph.getVertex(7)).isEmpty();
            assertThat(graph.getVertices())
                    .allSatisfy(v -> assertThat(v.getProperties().tags()).isEmpty());
            assertThat(graph.getDirectedEdges())
                    .allSatisfy(e -> assertThat(e.getProperties().tags()).isEmpty());
            assertThat(graph.getEdge(0, 6)).isPresent();
        }
    }
}
