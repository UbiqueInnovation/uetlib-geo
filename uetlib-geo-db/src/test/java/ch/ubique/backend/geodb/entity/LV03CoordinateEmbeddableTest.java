package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LV03CoordinateEmbeddableTest {

    @Test
    void toCoordinate() {
        final var lv03 = new LV03Coordinate(-1.2, 2.3);
        Assertions.assertEquals(lv03, LV03CoordinateEmbeddable.from(lv03).toCoordinate());
        final var lv03Elevation = new LV03Coordinate(-1, 2, 365.0158638);
        Assertions.assertEquals(
                lv03Elevation, LV03CoordinateEmbeddable.from(lv03Elevation).toCoordinate());
    }
}
