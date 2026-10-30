package ch.ubique.uetlib.geo.tile.smartdrawing.encoder;

import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

import ch.ubique.uetlib.geo.common.exceptions.UncheckedTransformException;
import ch.ubique.uetlib.geo.coordinate.models.IntCoordinate;
import ch.ubique.uetlib.geo.coordinate.util.JtsUtil;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class TileCoordinateEncoder {

    private final ReferencedEnvelope tileEnvelope;
    private final MathTransform tileTransform;

    private final double[] srcArray = new double[2];
    private final double[] dstArray = new double[2];

    public TileCoordinateEncoder(final int extent, final ReferencedEnvelope tileEnvelope) {
        this.tileEnvelope = tileEnvelope;
        try {
            this.tileTransform =
                    new AffineTransform2D(
                            RendererUtilities.worldToScreenTransform(
                                    tileEnvelope,
                                    new Rectangle(extent, extent),
                                    tileEnvelope.getCoordinateReferenceSystem()));
        } catch (final TransformException e) {
            throw new UncheckedTransformException(e);
        }
    }

    public List<IntCoordinate> computeTileCoordinates(final LineString lineString) {
        if (!JtsUtil.getCrs(lineString).equals(tileEnvelope.getCoordinateReferenceSystem())) {
            throw new IllegalArgumentException("CRS mismatch.");
        }

        final var tileCoordinates = new ArrayList<IntCoordinate>();
        for (final var coordinate : lineString.getCoordinates()) {
            final var tileCoordinate = computeTileCoordinate(coordinate);
            if (tileCoordinates.isEmpty() || !tileCoordinates.getLast().equals(tileCoordinate)) {
                tileCoordinates.add(tileCoordinate);
            }
        }
        return tileCoordinates;
    }

    public IntCoordinate computeTileCoordinate(final Coordinate coordinate) {
        try {
            srcArray[0] = coordinate.x;
            srcArray[1] = coordinate.y;
            tileTransform.transform(srcArray, 0, dstArray, 0, 1);

            final var elevation = coordinate.getZ();
            final var elevationCm =
                    Double.isFinite(elevation) ? toIntExact(round(elevation * 100)) : null;

            return new IntCoordinate(
                    toIntExact(round(dstArray[0])), toIntExact(round(dstArray[1])), elevationCm);
        } catch (final TransformException e) {
            throw new UncheckedTransformException(e);
        }
    }
}
