package ch.uetlib.geo.graph.io.geopackage;

import static ch.uetlib.geo.coordinate.constants.CoordinateSystems.LV95;

import static org.assertj.core.api.Assertions.assertThat;

import ch.uetlib.geo.coordinate.models.WGS84Coordinate;
import ch.uetlib.geo.coordinate.util.JtsConverter;
import ch.uetlib.geo.graph.routing.RoutingGraphs;
import ch.uetlib.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.uetlib.geo.graph.routing.utils.RoutingGraphTileParser;
import ch.uetlib.geo.tile.common.constants.SwissConstants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
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

        toGeoPackage(graphs);
    }

    @ParameterizedTest
    @CsvSource({
        "47.337571,7.835650",
        "47.375705, 8.543627",
        "46.991919666859474, 9.853818179472947"
    })
    void testSwitzerlandMobilityAroundCoordinate(final double lat, final double lon)
            throws IOException {
        final var wgs84Coordinate = new WGS84Coordinate(lat, lon);
        final var coordinate = JtsConverter.LV95.transform(wgs84Coordinate).toJts();
        final var tileInfoMaybe =
                SwissConstants.swissTileDefinition()
                        .tileInfos(21)
                        .filter(tileInfo -> tileInfo.envelope().contains(coordinate))
                        .findFirst();
        assertThat(tileInfoMaybe).isPresent();
        final var tileInfo = tileInfoMaybe.get();
        assertThat(tileInfo.envelope().contains(coordinate)).isTrue();

        final var path =
                "https://routing-fixture.example.invalid/"
                        + "chmob/vectortiles/graph/2025-03-18/complete/wander/"
                        + tileInfo.path();

        try (final var inputStream = URI.create(path).toURL().openStream()) {
            final var tile = RoutingGraphTile.parseFrom(inputStream);
            final var graphs = new RoutingGraphTileParser().parse(tile, tileInfo).graphs();
            toGeoPackage(graphs);
        }
    }

    private static void toGeoPackage(final RoutingGraphs graphs) throws IOException {
        try (final var encoder = new RoutingGraphGeopackageEncoder<>(LV95)) {
            encoder.encode(graphs);
            assertThat(encoder.geoPackage()).isNotNull();
            final var file = encoder.geoPackage().getFile();
            assertThat(file).exists();
            System.out.println(file.getAbsolutePath());
        }
    }
}
