package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

class JtsConverterTest {

    @Test
    void createPointWgs84() {
        assertPoint(JtsConverter.createPoint(new WGS84Coordinate(5.3, 1.2)));
    }

    @Test
    void createPointWebMercator() {
        assertPoint(JtsConverter.createPoint(new WebMercatorCoordinate(1.2, 5.3)));
    }

    @Test
    void createPointLv03() {
        assertPoint(JtsConverter.createPoint(new LV03Coordinate(1.2, 5.3)));
    }

    @Test
    void createPointLv95() {
        assertPoint(JtsConverter.createPoint(new LV95Coordinate(1.2, 5.3)));
    }

    private void assertPoint(final Point point) {
        Assertions.assertEquals(1.2, point.getX());
        Assertions.assertEquals(5.3, point.getY());
    }

    @Test
    void wgs84ToJtsPoint() {
        assertPoint(new WGS84Coordinate(5.3, 1.2).toJtsPoint());
        assertPoint(new WGS84Coordinate(5.3, 1.2, 17.2).toJtsPoint());
    }

    @Test
    void webMercatorToJtsPoint() {
        assertPoint(new WebMercatorCoordinate(1.2, 5.3).toJtsPoint());
        assertPoint(new WebMercatorCoordinate(1.2, 5.3, 17.2).toJtsPoint());
    }

    @Test
    void lv03ToJtsPoint() {
        assertPoint(new LV03Coordinate(1.2, 5.3).toJtsPoint());
        assertPoint(new LV03Coordinate(1.2, 5.3, 17.2).toJtsPoint());
    }

    @Test
    void lv95ToJtsPoint() {
        assertPoint(new LV95Coordinate(1.2, 5.3).toJtsPoint());
        assertPoint(new LV95Coordinate(1.2, 5.3, 17.2).toJtsPoint());
    }

    @Test
    void toWgs84() {
        final var wgs84Coordinate = JtsConverter.toWgs84(JtsConverter.createPoint(new WGS84Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, wgs84Coordinate.getLatitude());
        Assertions.assertEquals(4.2, wgs84Coordinate.getLongitude());
        Assertions.assertNull(wgs84Coordinate.getElevation());
    }

    @Test
    void toWebMercator() {
        final var webMercator =
                JtsConverter.toWebMercator(JtsConverter.createPoint(new WebMercatorCoordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, webMercator.getX());
        Assertions.assertEquals(4.2, webMercator.getY());
        Assertions.assertNull(webMercator.getElevation());
    }

    @Test
    void toLv03() {
        final var lv03Coordinate = JtsConverter.toLv03(JtsConverter.createPoint(new LV03Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, lv03Coordinate.getEast());
        Assertions.assertEquals(4.2, lv03Coordinate.getNorth());
        Assertions.assertNull(lv03Coordinate.getElevation());
    }

    @Test
    void toLv95() {
        final var lv95Coordinate = JtsConverter.toLv95(JtsConverter.createPoint(new LV95Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, lv95Coordinate.getEast());
        Assertions.assertEquals(4.2, lv95Coordinate.getNorth());
        Assertions.assertNull(lv95Coordinate.getElevation());
    }
}
