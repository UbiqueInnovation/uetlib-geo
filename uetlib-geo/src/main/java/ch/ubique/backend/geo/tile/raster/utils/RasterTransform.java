package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.core.utils.MathUtils;

import com.pivovarit.function.exception.WrappedException;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class RasterTransform {

    private RasterTransform() {}

    public static void transform(final Raster src, final WritableRaster dst, AffineTransform transform) {

        final var dstWidth = dst.getWidth();
        final var dstHeight = dst.getHeight();

        final AffineTransform inverseTransform;
        try {
            inverseTransform = transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new WrappedException(e);
        }

        final var srcPoint = new Point2D.Double();
        final var dstPoint = new Point2D.Double();
        final var result = new double[dst.getNumBands()];

        for (var x = 0; x < dstWidth; ++x) {
            for (var y = 0; y < dstHeight; ++y) {
                dstPoint.setLocation(x, y);
                inverseTransform.transform(dstPoint, srcPoint);
                if (withinBounds(src, srcPoint)) {
                    interpolate(src, srcPoint, result);
                    dst.setPixel(x, y, result);
                }
            }
        }
    }

    private static boolean withinBounds(Raster src, Point2D.Double srcPoint) {
        return Math.ceil(srcPoint.x) >= 0 &&
                Math.ceil(srcPoint.y) >= 0 &&
                Math.floor(srcPoint.x) < src.getWidth() &&
                Math.floor(srcPoint.y) < src.getHeight();
    }

    public static void interpolate(final Raster src, final Point2D sample, double[] result) {

        final var xSample = sample.getX();
        final var ySample = sample.getY();

        final var x1 = MathUtils.clamp((int) Math.floor(xSample), 0, src.getWidth() - 1);
        final var x2 = MathUtils.clamp((int) Math.ceil(xSample), 0, src.getWidth() - 1);
        final var y1 = MathUtils.clamp((int) Math.floor(ySample), 0, src.getHeight() - 1);
        final var y2 = MathUtils.clamp((int) Math.ceil(ySample), 0, src.getHeight() - 1);

        final var tx = xSample - x1;
        final var ty = ySample - y1;

        final var p11 = src.getPixel(x1, y1, (double[]) null);
        final var p12 = src.getPixel(x1, y2, (double[]) null);
        final var p21 = src.getPixel(x2, y1, (double[]) null);
        final var p22 = src.getPixel(x2, y2, (double[]) null);

        for (var i = 0; i < result.length; ++i) {
            result[i] = (1 - tx) * ((1 - ty) * p11[i] +  ty * p12[i]) + tx * ((1 - ty) * p21[i] + ty * p22[i]);
        }
    }
}
