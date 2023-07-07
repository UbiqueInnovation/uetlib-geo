package ch.ubique.backend.geo.geometry.exception;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public record SerializableEnvelope(@NotNull Coordinate min, @NotNull Coordinate max) {
    public SerializableEnvelope(@NotNull final Envelope envelope) {
        this(
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY()));
    }

    public SerializableEnvelope(@NotNull final Geometry geometry) {
        this(geometry.getEnvelopeInternal());
    }

    public <C extends ch.ubique.backend.geo.coordinate.models.Coordinate> SerializableEnvelope(
            @NotNull final BoundingBox<C> boundingBox) {
        this(boundingBox.min().toJts(), boundingBox.max().toJts());
    }
}
