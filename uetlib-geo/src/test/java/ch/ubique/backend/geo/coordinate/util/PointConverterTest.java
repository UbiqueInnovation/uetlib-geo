package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Point3d;
import org.junit.jupiter.api.Test;


public class PointConverterTest {

    @Test
    void testPointConverter() {
        var coordinate = new LV95Coordinate(2600000, 1200000, 400.);

        var point2d  = PointConverter.toPoint(Point2d.class, coordinate);
        var point3d = PointConverter.toPoint(Point3d.class, coordinate);

        System.out.println(point2d);
        System.out.println(point3d);
    }

}
