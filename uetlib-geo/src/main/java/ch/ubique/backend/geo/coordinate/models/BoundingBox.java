package ch.ubique.backend.geo.coordinate.models;

import org.jspecify.annotations.NonNull;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;

public record BoundingBox<C extends Coordinate>(@NonNull C min, @NonNull C max) {

    public ReferencedEnvelope toJts() {
        return new ReferencedEnvelope(new Envelope(min.toJts(), max.toJts()), min.crs());
    }
}
