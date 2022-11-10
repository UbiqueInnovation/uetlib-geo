package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;

import java.util.Random;
import java.util.stream.IntStream;

class TileGeometryUtilsTest {

    @Test
    void test() throws FactoryException {

        final var east = 2600000;
        final var north = 1200000;
        final var extend = 1000;
        final var tileSize = 100;
        final var tileEnvelope = new Envelope(east, east + tileSize, north, north + tileSize);

        final var random = new Random();

        for (var i : IntStream.range(0, 1000).toArray()) {
            final var testCoordinate = new LV95Coordinate(east + random.nextInt(-tileSize, 2 * tileSize), north + random.nextInt(-tileSize, 2 * tileSize));

            for (final var direction : YAxisDirection.values()) {
                @SuppressWarnings("deprecation") // This unit tests compares the behaviour of the existing code to the new code
                final var expected = TileGeometryUtils.getCoordinateOnTile(testCoordinate.toJtsPoint().getCoordinate(), tileEnvelope, extend, direction);
                final var actual = TileGeometryUtils.getTileGeometry(testCoordinate.toJtsPoint(), tileEnvelope, extend, direction);
                Assertions.assertTrue(actual.isPresent());
                Assertions.assertEquals(expected, actual.get().getCoordinate());
            }
        }
    }
}
