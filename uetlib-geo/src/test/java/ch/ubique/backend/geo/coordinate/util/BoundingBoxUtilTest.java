package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.models.*;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class BoundingBoxUtilTest {

    @Test
    void findBoundingBoxLv95() {
        final var c1 = Stream.of(new LV95Coordinate(1, 2), new LV95Coordinate(3, 4));
        final var b1 = new BoundingBox<>(new LV95Coordinate(1, 2), new LV95Coordinate(3, 4));
        assertEquals(b1, BoundingBoxUtil.findBoundingBox(c1).orElseThrow());

        final var c2 = Stream.of(new LV95Coordinate(1, 2), new LV95Coordinate(1, 4));
        final var b2 = new BoundingBox<>(new LV95Coordinate(1, 2), new LV95Coordinate(1, 4));
        assertEquals(b2, BoundingBoxUtil.findBoundingBox(c2).orElseThrow());

        final var c3 = Stream.of(new LV95Coordinate(1, 2), new LV95Coordinate(1, 1));
        final var b3 = new BoundingBox<>(new LV95Coordinate(1, 1), new LV95Coordinate(1, 2));
        assertEquals(b3, BoundingBoxUtil.findBoundingBox(c3).orElseThrow());

        final var c4 = Stream.<LV95Coordinate>of();
        assertTrue(BoundingBoxUtil.findBoundingBox(c4).isEmpty());
    }

    @Test
    void findBoundingBoxLv03() {
        final var c1 = Stream.of(new LV03Coordinate(1, 2), new LV03Coordinate(3, 4));
        final var b1 = new BoundingBox<>(new LV03Coordinate(1, 2), new LV03Coordinate(3, 4));
        assertEquals(b1, BoundingBoxUtil.findBoundingBox(c1).orElseThrow());

        final var c2 = Stream.of(new LV03Coordinate(1, 2), new LV03Coordinate(1, 4));
        final var b2 = new BoundingBox<>(new LV03Coordinate(1, 2), new LV03Coordinate(1, 4));
        assertEquals(b2, BoundingBoxUtil.findBoundingBox(c2).orElseThrow());

        final var c3 = Stream.of(new LV03Coordinate(1, 2), new LV03Coordinate(1, 1));
        final var b3 = new BoundingBox<>(new LV03Coordinate(1, 1), new LV03Coordinate(1, 2));
        assertEquals(b3, BoundingBoxUtil.findBoundingBox(c3).orElseThrow());

        final var c4 = Stream.<LV03Coordinate>of();
        assertTrue(BoundingBoxUtil.findBoundingBox(c4).isEmpty());
    }

    @Test
    void findBoundingBoxWgs84() {
        final var c1 = Stream.of(new WGS84Coordinate(1, 2), new WGS84Coordinate(3, 4));
        final var b1 = new BoundingBox<>(new WGS84Coordinate(1, 2), new WGS84Coordinate(3, 4));
        assertEquals(b1, BoundingBoxUtil.findBoundingBox(c1).orElseThrow());

        final var c2 = Stream.of(new WGS84Coordinate(1, 2), new WGS84Coordinate(1, 4));
        final var b2 = new BoundingBox<>(new WGS84Coordinate(1, 2), new WGS84Coordinate(1, 4));
        assertEquals(b2, BoundingBoxUtil.findBoundingBox(c2).orElseThrow());

        final var c3 = Stream.of(new WGS84Coordinate(1, 2), new WGS84Coordinate(1, 1));
        final var b3 = new BoundingBox<>(new WGS84Coordinate(1, 1), new WGS84Coordinate(1, 2));
        assertEquals(b3, BoundingBoxUtil.findBoundingBox(c3).orElseThrow());

        final var c4 = Stream.<WGS84Coordinate>of();
        assertTrue(BoundingBoxUtil.findBoundingBox(c4).isEmpty());
    }

    @Test
    void findBoundingBoxWebMercator() {
        final var c1 = Stream.of(new WebMercatorCoordinate(1, 2), new WebMercatorCoordinate(3, 4));
        final var b1 = new BoundingBox<>(new WebMercatorCoordinate(1, 2), new WebMercatorCoordinate(3, 4));
        assertEquals(b1, BoundingBoxUtil.findBoundingBox(c1).orElseThrow());

        final var c2 = Stream.of(new WebMercatorCoordinate(1, 2), new WebMercatorCoordinate(1, 4));
        final var b2 = new BoundingBox<>(new WebMercatorCoordinate(1, 2), new WebMercatorCoordinate(1, 4));
        assertEquals(b2, BoundingBoxUtil.findBoundingBox(c2).orElseThrow());

        final var c3 = Stream.of(new WebMercatorCoordinate(1, 2), new WebMercatorCoordinate(1, 1));
        final var b3 = new BoundingBox<>(new WebMercatorCoordinate(1, 1), new WebMercatorCoordinate(1, 2));
        assertEquals(b3, BoundingBoxUtil.findBoundingBox(c3).orElseThrow());

        final var c4 = Stream.<WebMercatorCoordinate>of();
        assertTrue(BoundingBoxUtil.findBoundingBox(c4).isEmpty());
    }
}
