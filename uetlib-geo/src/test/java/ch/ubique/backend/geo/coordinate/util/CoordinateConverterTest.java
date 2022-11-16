package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateConverterTest {

    @Test
    void testLV03LV95() {
        var bernLv03 = new LV03Coordinate(600000, 200000, 500.0);
        var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);

        assertEquals(bernLv03, JtsConverter.LV03.transform(bernLv95));
        assertEquals(bernLv95, JtsConverter.LV95.transform(bernLv03));
    }

    @Test
    void testLV95WGS84() {
        var bernLv95 = new LV95Coordinate(2600000, 1200000, 500.0);
        var bernWgs84 = new WGS84Coordinate(46.9510827861504654, 7.4386324175389165, 500.0);

        assertEquals(bernWgs84, JtsConverter.WGS84.transform(bernLv95));
        assertEquals(bernLv95, JtsConverter.LV95.transform(bernWgs84));
    }

    @Test
    void testWebMercatorWGS84() {
        var webMercator = new WebMercatorCoordinate(958207.0, 5847040.0, 500.0);
        var wgs84 = new WGS84Coordinate(46.4146005, 8.60772, 500.0);

        assertEquals(wgs84, JtsConverter.WGS84.transform((webMercator)));
    }
}
