package ch.uetlib.geo.coordinate.models;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;

public record BoundingBox<C extends Coordinate>(C min, C max) {

    public ReferencedEnvelope toJts() {
        return new ReferencedEnvelope(new Envelope(min.toJts(), max.toJts()), min.crs());
    }
}
