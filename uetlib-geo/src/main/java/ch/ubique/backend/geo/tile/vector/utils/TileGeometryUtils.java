package ch.ubique.backend.geo.tile.vector.utils;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Optional;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileGeometryUtils {

    private static final Logger logger = LoggerFactory.getLogger(TileGeometryUtils.class);

    private TileGeometryUtils() {}

    public static Optional<Geometry> getTileGeometry(
            final Geometry geometry,
            final ReferencedEnvelope tileEnvelope,
            final VectorTileConfig config) {

        ensureCrsMatch(geometry, tileEnvelope);

        try {
            final var withHeight = !Double.isNaN(geometry.getCoordinate().getZ());
            final var transformed =
                    JTS.transform(geometry, tileTransform(tileEnvelope, config, withHeight));
            return Optional.of(transformed);
        } catch (TransformException e) {
            logger.warn("Could not transform geometry for tile", e);
            return Optional.empty();
        }
    }

    private static void ensureCrsMatch(
            final Geometry geometry, final ReferencedEnvelope tileEnvelope) {
        final var crs = getCrs(geometry);
        if (!crs.equals(tileEnvelope.getCoordinateReferenceSystem())) {
            throw new MismatchedReferenceSystemException(
                    String.format(
                            "The elements (%s) crs (%s) does not match the tiles crs (%s)",
                            geometry, crs, tileEnvelope.getCoordinateReferenceSystem()));
        }
    }

    public static MathTransform tileTransform(
            final ReferencedEnvelope tileEnvelope,
            final VectorTileConfig config,
            boolean withHeight)
            throws TransformException {
        final var extent = config.getExtent();
        final var tileArea = new Rectangle(extent, extent);
        final var affineTransform = new AffineTransform();

        if (config.getYAxisDirection() == YAxisDirection.UP) {
            affineTransform.translate(0, extent);
            affineTransform.scale(1, -1);
        }

        affineTransform.concatenate(
                RendererUtilities.worldToScreenTransform(
                        tileEnvelope, tileArea, tileEnvelope.getCoordinateReferenceSystem()));

        final var transform2d = new AffineTransform2D(affineTransform);

        if (withHeight) {
            // The pass through transform ensures, that the height is passed through to the tile.
            // Otherwise, the height would just be dropped, i.e. 3D coordinates would be converted
            // to 2D coordinates
            return PassThroughTransform.create(0, transform2d, 1);
        }

        return transform2d;
    }
}
