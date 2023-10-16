package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;

public class GenericDistanceCalculator implements DistanceCalculator {
    private final GeodeticCalculator geodeticCalculator;

    private final MathTransform geographicTransform;
    private Coordinate start;
    private Coordinate end;

    public GenericDistanceCalculator(final CoordinateReferenceSystem crs) {
        this.geodeticCalculator = new GeodeticCalculator(crs);
        try {
            this.geographicTransform = CRS.findMathTransform(crs, CoordinateSystems.WGS84.crs());
        } catch (final FactoryException e) {
            throw new WrappedException(
                    e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    @Override
    public double getDistance(final Coordinate c1, final Coordinate c2) {
        try {
            start = JTS.transform(c1, start, geographicTransform);
            end = JTS.transform(c2, end, geographicTransform);
            geodeticCalculator.setStartingGeographicPoint(start.x, start.y);
            geodeticCalculator.setDestinationGeographicPoint(end.x, end.y);
            return geodeticCalculator.getOrthodromicDistance();
        } catch (final TransformException e) {
            throw new WrappedException(
                    e); // rethrow as runtime exception. no way for caller to handle this
        }
    }
}
