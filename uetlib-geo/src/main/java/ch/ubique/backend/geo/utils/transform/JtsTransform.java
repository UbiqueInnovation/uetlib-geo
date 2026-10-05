package ch.ubique.backend.geo.utils.transform;

import ch.ubique.backend.geo.common.exceptions.UncheckedTransformException;

import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class JtsTransform {

    private final MathTransform transform;

    public JtsTransform(final MathTransform transform) {
        this.transform = transform;
    }

    public Coordinate transform(final Coordinate coordinate) {
        try {
            return JTS.transform(coordinate, null, transform);
        } catch (final TransformException e) {
            throw new UncheckedTransformException(e);
        }
    }

    public Geometry transform(final Geometry geometry) {
        try {
            return JTS.transform(geometry, transform);
        } catch (final TransformException e) {
            throw new UncheckedTransformException(e);
        }
    }
}
