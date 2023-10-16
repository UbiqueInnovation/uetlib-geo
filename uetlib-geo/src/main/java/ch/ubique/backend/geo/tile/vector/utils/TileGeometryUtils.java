package ch.ubique.backend.geo.tile.vector.utils;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.api.geometry.MismatchedReferenceSystemException;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Geometry;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class TileGeometryUtils {

    private TileGeometryUtils() {}

    public static Geometry getTileGeometry(
            final Geometry geometry,
            final ReferencedEnvelope tileEnvelope,
            final VectorTileConfig config) {

        ensureCrsMatch(geometry, tileEnvelope);
        ensureCoordinatesAreWellDefined(geometry);

        try {
            final var withHeight = !Double.isNaN(geometry.getCoordinate().getZ());
            return JTS.transform(geometry, tileTransform(tileEnvelope, config, withHeight));
        } catch (final TransformException e) {
            throw new WrappedException("Could not transform geometry for tile", e);
        }
    }

    public static void ensureCrsMatch(
            final Geometry geometry, final ReferencedEnvelope tileEnvelope) {
        final var crs = getCrs(geometry);
        if (!crs.equals(tileEnvelope.getCoordinateReferenceSystem())) {
            throw new MismatchedReferenceSystemException(
                    String.format(
                            "The elements (%s) crs (%s) does not match the tiles crs (%s)",
                            geometry, crs, tileEnvelope.getCoordinateReferenceSystem()));
        }
    }

    private static void ensureCoordinatesAreWellDefined(final Geometry geometry) {
        final var crs = getCrs(geometry);
        final var coordinateSystemBounds = CoordinateSystems.forCrs(crs).bounds();
        Arrays.stream(geometry.getCoordinates())
                .forEach(
                        coordinate -> {
                            if (!coordinateSystemBounds.covers(coordinate)) {
                                throw new IllegalArgumentException(
                                        "The given geometry has a coordinate outside the defined"
                                                + " bounds of the coordinate system. Violating"
                                                + " coordinate: "
                                                + coordinate);
                            }
                        });
    }

    public static MathTransform tileTransform(
            final ReferencedEnvelope tileEnvelope,
            final VectorTileConfig config,
            final boolean withHeight)
            throws TransformException {
        final var extent = config.extent();
        final var tileArea = new Rectangle(extent, extent);
        final var affineTransform = new AffineTransform();

        if (config.yAxisDirection() == YAxisDirection.UP) {
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
