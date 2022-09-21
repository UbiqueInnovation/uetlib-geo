package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;

import org.junit.jupiter.api.Test;

class CoordinateConverterTest {

    @Test
    void testLV03LV95() {
        var bernLv03 = new LV03Coordinate(600000, 200000);
        var bernLv95 = new LV95Coordinate(2600000, 1200000);

        assertEquals(bernLv03, CoordinateConverter.toLV03(bernLv95));
        assertEquals(bernLv95, CoordinateConverter.toLV95(bernLv03));
    }

    @Test
    void testLV95WGS84() {
        var bernLv95 = new LV95Coordinate(2600000, 1200000);
        var bernWgs84 = new WGS84Coordinate(46.9510827861504654, 7.4386324175389165);

        assertEquals(bernWgs84, CoordinateConverter.toWGS84(bernLv95));
        assertEquals(bernLv95, CoordinateConverter.toLV95(bernWgs84));
    }

    @Test
    void testWebMercatorWGS84() {
        var webMercator = new WebMercatorCoordinate(958207.0, 5847040.0);
        var wgs84 = new WGS84Coordinate(46.4146005, 8.60772);

        assertEquals(wgs84, CoordinateConverter.toWGS84(webMercator));
    }
}
