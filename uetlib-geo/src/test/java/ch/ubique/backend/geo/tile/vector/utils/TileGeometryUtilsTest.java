package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.util.Random;
import java.util.stream.IntStream;

class TileGeometryUtilsTest {

    @Test
    void test() {

        final var east = 2600000;
        final var north = 1200000;
        final var extend = 1000;
        final var tileSize = 100;
        final var tileEnvelope = new Envelope(east, east + tileSize, north, north + tileSize);

        final var random = new Random();

        for (var i : IntStream.range(0, 1000).toArray()) {
            final var testCoordinate = new LV95Coordinate(
                    east + random.nextInt(-tileSize, 2 * tileSize),
                    north + random.nextInt(-tileSize, 2 * tileSize),
                    random.nextDouble(0, 4000));

            for (final var direction : YAxisDirection.values()) {
                final var actual = TileGeometryUtils.getTileGeometry(JtsConverter.LV95.toJtsPoint(testCoordinate), tileEnvelope, extend, direction);
                Assertions.assertTrue(actual.isPresent());
                Assertions.assertEquals(testCoordinate.getElevation().orElse(null), actual.get().getCoordinate().getZ());
            }
        }
    }
}
