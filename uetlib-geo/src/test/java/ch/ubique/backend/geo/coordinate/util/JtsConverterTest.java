package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class JtsConverterTest {

    @Test
    void createPointWgs84() {
        assertPoint(JtsConverter.WGS84.toJts(new WGS84Coordinate(5.3, 1.2)));
    }

    @Test
    void createPointWebMercator() {
        assertPoint(JtsConverter.WEB_MERCATOR.toJts(new WebMercatorCoordinate(1.2, 5.3)));
    }

    @Test
    void createPointLv03() {
        assertPoint(JtsConverter.LV03.toJts(new LV03Coordinate(1.2, 5.3)));
    }

    @Test
    void createPointLv95() {
        assertPoint(JtsConverter.LV95.toJts(new LV95Coordinate(1.2, 5.3)));
    }

    private void assertPoint(final Coordinate point) {
        Assertions.assertEquals(1.2, point.getX());
        Assertions.assertEquals(5.3, point.getY());
    }

    @Test
    void wgs84toJts() {
        assertPoint(new WGS84Coordinate(5.3, 1.2).toJts());
        assertPoint(new WGS84Coordinate(5.3, 1.2, 17.2).toJts());
    }

    @Test
    void webMercatortoJts() {
        assertPoint(new WebMercatorCoordinate(1.2, 5.3).toJts());
        assertPoint(new WebMercatorCoordinate(1.2, 5.3, 17.2).toJts());
    }

    @Test
    void lv03toJts() {
        assertPoint(new LV03Coordinate(1.2, 5.3).toJts());
        assertPoint(new LV03Coordinate(1.2, 5.3, 17.2).toJts());
    }

    @Test
    void lv95toJts() {
        assertPoint(new LV95Coordinate(1.2, 5.3).toJts());
        assertPoint(new LV95Coordinate(1.2, 5.3, 17.2).toJts());
    }

    @Test
    void toWgs84() {
        final var wgs84Coordinate = JtsConverter.WGS84.toBackendLib(JtsConverter.WGS84.toJts(new WGS84Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, wgs84Coordinate.getLatitude());
        Assertions.assertEquals(4.2, wgs84Coordinate.getLongitude());
        Assertions.assertTrue(wgs84Coordinate.getElevation().isEmpty());
    }

    @Test
    void toWebMercator() {
        final var webMercator =
                JtsConverter.WEB_MERCATOR.toBackendLib(JtsConverter.WEB_MERCATOR.toJts(new WebMercatorCoordinate(2.8, 4.2, 17.2)));
        Assertions.assertEquals(2.8, webMercator.getX());
        Assertions.assertEquals(4.2, webMercator.getY());
        Assertions.assertTrue(webMercator.getElevation().isPresent());
    }

    @Test
    void toLv03() {
        final var lv03Coordinate = JtsConverter.LV03.toBackendLib(JtsConverter.LV03.toJts(new LV03Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, lv03Coordinate.getEast());
        Assertions.assertEquals(4.2, lv03Coordinate.getNorth());
        Assertions.assertTrue(lv03Coordinate.getElevation().isEmpty());
    }

    @Test
    void toLv95() {
        final var lv95Coordinate = JtsConverter.LV95.toBackendLib(JtsConverter.LV95.toJts(new LV95Coordinate(2.8, 4.2)));
        Assertions.assertEquals(2.8, lv95Coordinate.getEast());
        Assertions.assertEquals(4.2, lv95Coordinate.getNorth());
        Assertions.assertTrue(lv95Coordinate.getElevation().isEmpty());
    }
}
