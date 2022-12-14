package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import java.util.Random;
import java.util.stream.IntStream;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileGeometryUtilsTest {

    @Test
    void test() {

        final var east = 2600000;
        final var north = 1200000;
        final var extend = 1000;
        final var tileSize = 100;
        final var config = VectorTileConfig.lv95().setExtent(extend);
        final var tileEnvelope = new Envelope(east, east + tileSize, north, north + tileSize);

        final var random = new Random();

        for (var i : IntStream.range(0, 1000).toArray()) {
            final var testCoordinate =
                    new LV95Coordinate(
                            east + random.nextInt(-tileSize, 2 * tileSize),
                            north + random.nextInt(-tileSize, 2 * tileSize),
                            random.nextDouble(0, 4000));

            for (final var direction : YAxisDirection.values()) {
                final var actual =
                        TileGeometryUtils.getTileGeometry(
                                testCoordinate.toJtsPoint(),
                                new ReferencedEnvelope(tileEnvelope, testCoordinate.crs()),
                                config);
                Assertions.assertFalse(actual.isEmpty());
                Assertions.assertEquals(
                        testCoordinate.getOptionalElevation().orElse(null),
                        actual.getCoordinate().getZ());
            }
        }
    }
}
