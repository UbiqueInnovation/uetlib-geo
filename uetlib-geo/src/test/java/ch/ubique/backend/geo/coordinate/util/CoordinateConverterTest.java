package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;

import org.junit.jupiter.api.Test;

class CoordinateConverterTest {
    private static final double LV03_EPSILON = 0.1;
    private static final double LV95_EPSILON = 0.1;
    private static final double WGS84_EPSILON = 0.001;
    private static final double WEB_MERCATOR_EPSILON = 0.1;

    @Test
    void testLV03LV95() {
        final var bernLv03 = new LV03Coordinate(600000, 200000, 500.0);
        final var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);

        assertTrue(bernLv03.distanceTo(JtsConverter.LV03.transform(bernLv95)) < LV03_EPSILON);
        assertTrue(bernLv95.distanceTo(JtsConverter.LV95.transform(bernLv03)) < LV95_EPSILON);
    }

    @Test
    void testLV95WGS84() {
        final var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);
        final var bernWgs84 = new WGS84Coordinate(46.9510827861504654, 7.4386324175389165, 500.0);

        assertTrue(bernWgs84.distanceTo(JtsConverter.WGS84.transform(bernLv95)) < WGS84_EPSILON);
        assertTrue(bernLv95.distanceTo(JtsConverter.LV95.transform(bernWgs84)) < LV95_EPSILON);
    }

    @Test
    void testWebMercatorWGS84() {
        final var webMercator = new WebMercatorCoordinate(958207.0, 5847040.0, 500.0);
        final var wgs84 = new WGS84Coordinate(46.4146005, 8.60772, 500.0);

        assertTrue(wgs84.distanceTo(JtsConverter.WGS84.transform(webMercator)) < WGS84_EPSILON);
    }
}
