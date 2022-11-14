package ch.ubique.backend.geo.geometry.utils;

import static ch.ubique.backend.geo.geometry.utils.GeometryHelper.getCrs;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class GeometryConverter {
    public static Geometry toLv95(Geometry geometry) {
        try {
            final CoordinateReferenceSystem lv95Crs =
                    CRS.decode("EPSG:" + SwissConstants.LV95_SRID);
            final Geometry lv95geom =
                    JTS.transform(geometry, CRS.findMathTransform(getCrs(geometry), lv95Crs));
            // JTS.transform doesnt update srid
            lv95geom.setSRID(SwissConstants.LV95_SRID);
            return lv95geom;
        } catch (FactoryException | TransformException e) {
            // rethrow as runtime exception. no way for caller to handle this
            throw new RuntimeException(e);
        }
    }
}
