package ch.uetlib.geo.tile.common.models;

import ch.uetlib.geo.tile.raster.utils.MapboxTerrainRgb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

class MapboxTerrainRgbTest {

    @Test
    void test() {
        final var tests = 1000;
        final var random = new Random();

        IntStream.range(0, tests)
                .forEach(x -> {
                    final var randomInt = random.nextInt(16777216);
                    Assertions.assertEquals(
                            randomInt,
                            MapboxTerrainRgb.decodeValue(MapboxTerrainRgb.encodeValue(randomInt)));
                });

        IntStream.range(0, tests)
                .forEach(x -> {
                    final var randomInts =
                            new int[] {
                                random.nextInt(255), random.nextInt(255), random.nextInt(255)
                            };
                    Assertions.assertArrayEquals(
                            randomInts,
                            MapboxTerrainRgb.encodeValue(MapboxTerrainRgb.decodeValue(randomInts)));
                });

        IntStream.range(0, tests)
                .forEach(x -> {
                    final var randomElevation = random.nextFloat(-10000, 10000);
                    Assertions.assertEquals(
                            randomElevation,
                            MapboxTerrainRgb.decode(MapboxTerrainRgb.encode(randomElevation)),
                            0.1);
                });
    }
}
