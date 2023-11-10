package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

public sealed interface Coordinate
        permits LV03Coordinate,
                LV95Coordinate,
                UTM32Coordinate,
                WGS84Coordinate,
                WebMercatorCoordinate {

    default int srid() {
        return CoordinateSystems.forClass(getClass()).srid();
    }

    default CoordinateReferenceSystem crs() {
        return CoordinateSystems.forClass(getClass()).crs();
    }

    org.locationtech.jts.geom.Coordinate toJts();

    default org.locationtech.jts.geom.Point toJtsPoint() {
        return CoordinateSystems.forClass(getClass()).geometryFactory().createPoint(toJts());
    }
}
