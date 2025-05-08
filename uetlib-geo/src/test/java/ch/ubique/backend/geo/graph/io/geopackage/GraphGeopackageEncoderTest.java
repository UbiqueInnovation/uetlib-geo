package ch.ubique.backend.geo.graph.io.geopackage;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.LV95;

import static org.assertj.core.api.Assertions.assertThat;

import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphTileParser;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

class GraphGeopackageEncoderTest {

    @Test
    void test() throws IOException {
        final var tileDefinitions = SwissConstants.swissTileDefinition();
        final var parser = new RoutingGraphTileParser();
        for (final var tileInfo :
                List.of(
                        tileDefinitions.tileInfo(30, 89, 20),
                        tileDefinitions.tileInfo(30, 90, 20),
                        tileDefinitions.tileInfo(30, 91, 20),
                        tileDefinitions.tileInfo(31, 90, 20),
                        tileDefinitions.tileInfo(31, 91, 20))) {

            final var resourcePath =
                    String.format(
                            "/routing_graph/complete_%d_%d_%d.pbf.gz",
                            tileInfo.level(), tileInfo.x(), tileInfo.y());
            final var resource = getClass().getResourceAsStream(resourcePath);
            assertThat(resource).withFailMessage(() -> resourcePath).isNotNull();
            final var tile = RoutingGraphTile.parseFrom(new GZIPInputStream(resource));
            parser.parse(tile, tileInfo);
        }
        final var graphs = parser.graphs();

        try (final var encoder = new RoutingGraphGeopackageEncoder<>(LV95)) {
            encoder.encode(graphs);
            assertThat(encoder.geoPackage()).isNotNull();
            final var file = encoder.geoPackage().getFile();
            assertThat(file).exists();
            System.out.println(file.getAbsolutePath());
        }
    }
}
