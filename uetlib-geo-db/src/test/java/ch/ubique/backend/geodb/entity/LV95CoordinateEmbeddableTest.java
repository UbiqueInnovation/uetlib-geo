package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LV95CoordinateEmbeddableTest {

    @Test
    void toCoordinate() {
        final var lv95 = new LV95Coordinate(-1.2, 2.3);
        Assertions.assertEquals(lv95, LV95CoordinateEmbeddable.from(lv95).toCoordinate());
        final var lv95Elevation = new LV95Coordinate(-1, 2, 365.0158638);
        Assertions.assertEquals(
                lv95Elevation, LV95CoordinateEmbeddable.from(lv95Elevation).toCoordinate());
    }
}
