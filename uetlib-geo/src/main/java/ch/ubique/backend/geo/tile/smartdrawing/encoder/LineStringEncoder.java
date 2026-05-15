package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static java.lang.Math.round;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;

import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.LineString;

import java.awt.Rectangle;

public class LineStringEncoder {

    private final ReferencedEnvelope tileEnvelope;
    private final MathTransform tileTransform;

    public LineStringEncoder(final int extent, final ReferencedEnvelope tileEnvelope)
            throws TransformException {
        this.tileEnvelope = tileEnvelope;
        this.tileTransform =
                new AffineTransform2D(
                        RendererUtilities.worldToScreenTransform(
                                tileEnvelope,
                                new Rectangle(extent, extent),
                                tileEnvelope.getCoordinateReferenceSystem()));
    }

    public CoordinateList computeTileCoordinates(final LineString lineString)
            throws TransformException {
        if (!JtsUtil.getCrs(lineString).equals(tileEnvelope.getCoordinateReferenceSystem())) {
            throw new IllegalArgumentException("CRS mismatch.");
        }

        final var tileCoordinates = new CoordinateList();

        final var srcArray = new double[2];
        final var dstArray = new double[2];
        for (final var coordinate : lineString.getCoordinates()) {
            srcArray[0] = coordinate.x;
            srcArray[1] = coordinate.y;
            tileTransform.transform(srcArray, 0, dstArray, 0, 1);

            final var elevationCm =
                    Double.isFinite(coordinate.getZ())
                            ? round(coordinate.getZ() * 100)
                            : Coordinate.NULL_ORDINATE;

            final var tileCoordinate =
                    new Coordinate(round(dstArray[0]), round(dstArray[1]), elevationCm);

            tileCoordinates.add(tileCoordinate, false);
        }
        return tileCoordinates;
    }
}
