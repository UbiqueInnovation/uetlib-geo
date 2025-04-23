package ch.ubique.backend.geo.graph.routing.utils;

import static ch.ubique.backend.geo.graph.algorithms.ConnectedComponents.computeComponents;

import static org.assertj.core.api.Assertions.assertThat;

import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

class RoutingGraphTileParserTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "complete_20_30_90.pbf.gz",
                "routing_20_30_90.pbf.gz",
                "highway_complete_18_16_17.pbf.gz",
                "highway_routing_18_16_17.pbf.gz"
            })
    void parseOneTile(final String file) throws IOException {
        final var resource = getClass().getResourceAsStream("/routing_graph/" + file);
        assertThat(resource).isNotNull();
        final var tile = RoutingGraphTile.parseFrom(new GZIPInputStream(resource));
        assertThat(tile).isNotNull();
        assertThat(tile.getVerticesCount()).isGreaterThan(2_000);
        assertThat(tile.getCoordinatesCount()).isGreaterThan(tile.getVerticesCount());
        final var graphs = new RoutingGraphTileParser().parse(tile).graphs();
        assertThat(graphs.completeGraph()).isNotNull();
        assertThat(graphs.routingGraph()).isNotNull();
        assertThat(graphs.completeGraph().getVertices())
                .hasSizeGreaterThanOrEqualTo(tile.getVerticesCount());
        assertThat(graphs.routingGraph().getVertices()).hasSizeGreaterThanOrEqualTo(2_000);
    }

    @Test
    void parseMultiple() throws IOException {
        final var parser = new RoutingGraphTileParser();
        for (final var file :
                List.of(
                        "complete_20_30_89.pbf.gz",
                        "complete_20_30_90.pbf.gz",
                        "complete_20_30_91.pbf.gz",
                        "complete_20_31_90.pbf.gz",
                        "complete_20_31_91.pbf.gz")) {
            final var resource = getClass().getResourceAsStream("/routing_graph/" + file);
            assertThat(resource).isNotNull();
            final var tile = RoutingGraphTile.parseFrom(new GZIPInputStream(resource));
            assertThat(tile).isNotNull();
            parser.parse(tile);
        }
        final var graphs = parser.graphs();
        assertThat(graphs.completeGraph()).isNotNull();
        assertThat(graphs.routingGraph()).isNotNull();
        assertThat(graphs.completeGraph().getVertices()).hasSize(72_073);
        assertThat(graphs.routingGraph().getVertices()).hasSize(11_037);
        assertThat(computeComponents(graphs.completeGraph()))
                .hasSizeGreaterThanOrEqualTo(computeComponents(graphs.routingGraph()).size());
    }
}
