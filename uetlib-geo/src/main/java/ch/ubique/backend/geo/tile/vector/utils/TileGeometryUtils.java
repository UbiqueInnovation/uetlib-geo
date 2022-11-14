package ch.ubique.backend.geo.tile.vector.utils;

import static ch.ubique.backend.geo.geometry.utils.GeometryHelper.getCrs;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Optional;

public class TileGeometryUtils {

    private static final Logger logger = LoggerFactory.getLogger(TileGeometryUtils.class);

    private TileGeometryUtils() {}

    public static Optional<Geometry> getTileGeometry(
            final Geometry geometry,
            final Envelope tileEnvelope,
            final int extend,
            final YAxisDirection yAxisDirection) throws FactoryException {

        try {
            final var crs = getCrs(geometry);
            final var transformed = JTS.transform(geometry, tileTransform(tileEnvelope, extend, yAxisDirection, crs));
            return Optional.of(transformed);
        } catch (TransformException e) {
            logger.warn("Could not transform geometry for tile", e);
            return Optional.empty();
        }
    }

    public static MathTransform tileTransform(final Envelope tileEnvelope, final int extend, YAxisDirection yAxisDirection, CoordinateReferenceSystem crs) throws TransformException {
        final var tileArea = new Rectangle(extend, extend);
        final var affineTransform = new AffineTransform();

        if (yAxisDirection == YAxisDirection.UP) {
            affineTransform.translate(0, extend);
            affineTransform.scale(1, -1);
        }

        affineTransform.concatenate(RendererUtilities.worldToScreenTransform(tileEnvelope, tileArea, crs));
        return new AffineTransform2D(affineTransform);
    }


    /**
     * @deprecated please use tileTransform instead
     */
    @Deprecated(since = "0.7.11")
    public static Coordinate getCoordinateOnTile(
            final Coordinate originalCoordinate,
            final Envelope tileEnvelope,
            final int extend,
            final YAxisDirection yAxisDirection) {

        double x =
                Math.round(
                        (originalCoordinate.getX() - tileEnvelope.getMinX())
                                / tileEnvelope.getWidth()
                                * extend);

        /*
         * currently y is only correct when using LV95 or LV03 coordinates, which increase in UPward
         * direction
         */
        double y =
                Math.round(
                        (originalCoordinate.getY() - tileEnvelope.getMinY())
                                / tileEnvelope.getHeight()
                                * extend);
        if (YAxisDirection.DOWN.equals(yAxisDirection)) {
            y = extend - y;
        }
        return new Coordinate(x, y);
    }

}
