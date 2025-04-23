package ch.ubique.backend.geo.graph.io.geopackage;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.LV95;

import static org.assertj.core.api.Assertions.assertThat;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphTileParser;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

class GraphGeopackageEncoderTest {

    @Test
    void test() throws IOException {
        final var resourcePath = "/routing_graph/complete_20_30_90.pbf.gz";
        final var resource = getClass().getResourceAsStream(resourcePath);
        assertThat(resource).isNotNull();
        final var tile = RoutingGraphTile.parseFrom(new GZIPInputStream(resource));
        final var graphs =
                new RoutingGraphTileParser()
                        .parse(tile, SwissConstants.swissTileDefinition().tileInfo(30, 90, 20))
                        .graphs();

        try (final var encoder = new GraphGeopackageEncoder<>(this::getCoordinate, LV95)) {
            encoder.encode(graphs.completeGraph());
            assertThat(encoder.geoPackage()).isNotNull();
            assertThat(encoder.file()).exists();
            System.out.println(encoder.file().getAbsolutePath());
        }
    }

    private LV95Coordinate getCoordinate(final RoutingGraphVertexProperties vertexProperties) {
        return LV95.fromJts(vertexProperties.coordinate());
    }
}
