package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ubique.backend.geo.coordinate.models.*;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculators;

import org.junit.jupiter.api.Test;

class CoordinateConverterTest {
    private static final double LV03_EPSILON = 0.1;
    private static final double LV95_EPSILON = 0.1;
    private static final double WGS84_EPSILON = 0.05;
    private static final double WEB_MERCATOR_EPSILON = 0.1;

    @Test
    void testLV03LV95() {
        final var bernLv03 = new LV03Coordinate(600000, 200000, 500.0);
        final var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);

        assertTrue(
                DistanceCalculators.LV03.getDistance(
                                bernLv03.toJts(), JtsConverter.LV03.transform(bernLv95).toJts())
                        < LV03_EPSILON);
        assertTrue(
                DistanceCalculators.LV95.getDistance(
                                bernLv95.toJts(), JtsConverter.LV95.transform(bernLv03).toJts())
                        < LV95_EPSILON);
    }

    @Test
    void testLV95WGS84() {
        final var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);
        final var bernWgs84 = new WGS84Coordinate(46.9510827861504654, 7.4386324175389165, 500.0);

        assertTrue(
                DistanceCalculators.WGS84.getDistance(
                                bernWgs84.toJts(), JtsConverter.WGS84.transform(bernLv95).toJts())
                        < WGS84_EPSILON);
        assertTrue(
                DistanceCalculators.WGS84.getDistance(
                                JtsConverter.WGS84.transform(bernLv95).toJts(), bernWgs84.toJts())
                        < WGS84_EPSILON);
    }

    @Test
    void testWebMercatorWGS84() {
        final var webMercator = new WebMercatorCoordinate(958207.0, 5847040.0, 500.0);
        final var wgs84 = new WGS84Coordinate(46.4146005, 8.60772, 500.0);

        assertTrue(
                DistanceCalculators.WGS84.getDistance(
                                wgs84.toJts(), JtsConverter.WGS84.transform(webMercator).toJts())
                        < WGS84_EPSILON);
    }

    @Test
    void testUtm32WGS84() {
        final var utm32 = new UTM32Coordinate(0, 0, 500.0);
        final var wgs84 = new WGS84Coordinate(0.0, 4.5112562, 500.0);

        assertTrue(
                DistanceCalculators.WGS84.getDistance(
                                wgs84.toJts(), JtsConverter.WGS84.transform(utm32).toJts())
                        < WGS84_EPSILON);
    }
}
