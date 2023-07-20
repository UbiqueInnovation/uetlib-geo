package ch.ubique.backend.geo.coordinate.models;

import jakarta.validation.constraints.NotNull;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;

public record BoundingBox<C extends Coordinate>(@NotNull C min, @NotNull C max) {

    public ReferencedEnvelope toJts() {
        return new ReferencedEnvelope(new Envelope(min.toJts(), max.toJts()), min.crs());
    }
}
