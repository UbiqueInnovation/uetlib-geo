package ch.ubique.uetlib.geo.coordinate.models;

import ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystem;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

public sealed interface Coordinate
        permits LV03Coordinate,
                LV95Coordinate,
                UTM32Coordinate,
                WGS84Coordinate,
                WebMercatorCoordinate {

    @SuppressWarnings("java:S1452")
    CoordinateSystem<? extends Coordinate> coordinateSystem();

    default int srid() {
        return coordinateSystem().srid();
    }

    default CoordinateReferenceSystem crs() {
        return coordinateSystem().crs();
    }

    org.locationtech.jts.geom.Coordinate toJts();

    default org.locationtech.jts.geom.Point toJtsPoint() {
        return coordinateSystem().geometryFactory().createPoint(toJts());
    }
}
