package ch.ubique.backend.geo.geometry.exception;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;

import org.jspecify.annotations.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public record SerializableEnvelope(@NonNull Coordinate min, @NonNull Coordinate max) {
    public SerializableEnvelope(@NonNull final Envelope envelope) {
        this(
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY()));
    }

    public SerializableEnvelope(@NonNull final Geometry geometry) {
        this(geometry.getEnvelopeInternal());
    }

    public <C extends ch.ubique.backend.geo.coordinate.models.Coordinate> SerializableEnvelope(
            @NonNull final BoundingBox<C> boundingBox) {
        this(boundingBox.min().toJts(), boundingBox.max().toJts());
    }
}
