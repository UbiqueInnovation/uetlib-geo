package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WGS84CoordinateEmbeddableTest {

    @Test
    void toCoordinate() {
        final var wgs84 = new WGS84Coordinate(-1.2, 2.3);
        Assertions.assertEquals(wgs84, WGS84CoordinateEmbeddable.from(wgs84).toCoordinate());
        final var wgs84Elevation = new WGS84Coordinate(-1, 2, 365.0158638);
        Assertions.assertEquals(
                wgs84Elevation, WGS84CoordinateEmbeddable.from(wgs84Elevation).toCoordinate());
    }
}
