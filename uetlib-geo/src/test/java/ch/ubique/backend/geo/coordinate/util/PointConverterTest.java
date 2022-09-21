package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Point3d;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointConverterTest {

    @Test
    void testPointConverter() {
        var coordinate = new LV95Coordinate(2600000, 1200000, 400.);

        var point2d = PointConverter.toPoint(Point2d.class, coordinate);
        var point3d = PointConverter.toPoint(Point3d.class, coordinate);

        Assertions.assertEquals(new Point2d(2600000, 1200000).toString(), point2d.toString());
        Assertions.assertEquals(new Point3d(2600000, 1200000, 400).toString(), point3d.toString());
    }
}
