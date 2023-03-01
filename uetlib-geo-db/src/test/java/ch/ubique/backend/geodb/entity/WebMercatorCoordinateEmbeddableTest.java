package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebMercatorCoordinateEmbeddableTest {

    @Test
    void toCoordinate() {
        final var webMercator = new WebMercatorCoordinate(-1.2, 2.3);
        Assertions.assertEquals(
                webMercator, WebMercatorCoordinateEmbeddable.from(webMercator).toCoordinate());
        final var webMercatorElevation = new WebMercatorCoordinate(-1, 2, 365.0158638);
        Assertions.assertEquals(
                webMercatorElevation,
                WebMercatorCoordinateEmbeddable.from(webMercatorElevation).toCoordinate());
    }
}
