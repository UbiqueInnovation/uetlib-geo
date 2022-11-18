package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Optional;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

public class TileGeometryUtils {

    private static final Logger logger = LoggerFactory.getLogger(TileGeometryUtils.class);

    private TileGeometryUtils() {}

    public static Optional<Geometry> getTileGeometry(
            final Geometry geometry,
            final Envelope tileEnvelope,
            final int extend,
            final YAxisDirection yAxisDirection) {

        try {
            final var crs = getCrs(geometry);
            final var withHeight = !Double.isNaN(geometry.getCoordinate().getZ());
            final var transformed = JTS.transform(geometry, tileTransform(tileEnvelope, extend, yAxisDirection, crs, withHeight));
            return Optional.of(transformed);
        } catch (TransformException e) {
            logger.warn("Could not transform geometry for tile", e);
            return Optional.empty();
        }
    }

    public static MathTransform tileTransform(final Envelope tileEnvelope, final int extend, YAxisDirection yAxisDirection, CoordinateReferenceSystem crs, boolean withHeight) throws TransformException {
        final var tileArea = new Rectangle(extend, extend);
        final var affineTransform = new AffineTransform();

        if (yAxisDirection == YAxisDirection.UP) {
            affineTransform.translate(0, extend);
            affineTransform.scale(1, -1);
        }

        affineTransform.concatenate(RendererUtilities.worldToScreenTransform(tileEnvelope, tileArea, crs));

        final var transform2d = new AffineTransform2D(affineTransform);

        if (withHeight) {
            // The pass through transform ensures, that the height is passed through to the tile.
            // Otherwise, the height would just be dropped, i.e. 3D coordinates would be converted to 2D coordinates
            return PassThroughTransform.create(0, transform2d, 1);
        }

        return transform2d;
    }
}
