package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class CoordinateConverterTest {

    @Test
    void testLV03LV95() {
        var bernLv03 = new LV03Coordinate(600000, 200000);
        var bernLv95 = new LV95Coordinate(2600000, 1200000);

        Assertions.assertEquals(bernLv03, CoordinateConverter.toLV03(bernLv95));
        Assertions.assertEquals(bernLv95, CoordinateConverter.toLV95(bernLv03));
    }

    @Test
    void testLV95WGS84() {
        var bernLv95 = new LV95Coordinate(2600000, 1200000);
        var bernWgs84 = new WGS84Coordinate(46.9510827861504654, 7.4386324175389165);

        Assertions.assertEquals(bernWgs84, CoordinateConverter.toWGS84(bernLv95));
        Assertions.assertEquals(bernLv95, CoordinateConverter.toLV95(bernWgs84));
    }
}
