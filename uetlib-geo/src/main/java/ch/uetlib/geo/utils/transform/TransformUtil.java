package ch.uetlib.geo.utils.transform;

import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.lite.RendererUtilities;

import java.awt.Rectangle;

public class TransformUtil {

    private TransformUtil() {}

    public static JtsTransform worldToEnvelopeJtsTransform(
            final ReferencedEnvelope tileEnvelope, final int extent) {
        return new JtsTransform(worldToEnvelopeMathTransform(tileEnvelope, extent));
    }

    public static MathTransform worldToEnvelopeMathTransform(
            final ReferencedEnvelope tileEnvelope, final int extent) {
        return new AffineTransform2D(
                RendererUtilities.worldToScreenTransform(
                        tileEnvelope, new Rectangle(extent, extent)));
    }
}
