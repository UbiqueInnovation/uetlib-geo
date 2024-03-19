package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;
import ch.ubique.backend.geo.tlm.models.TlmPath;

import org.locationtech.jts.geom.Geometry;

import java.util.Objects;
import java.util.Optional;

public record TlmTileElement(long id, Geometry geometry, TlmTags tags)
        implements VectorTileElementWithTags<TlmTags> {

    public TlmTileElement(final long id, final TlmPath path) {
        this(id, path.line(), new TlmTags(path.info()));
    }

    @Override
    public String layer() {
        return Optional.ofNullable(tags.trailType())
                .map(Objects::toString)
                .orElseGet(() -> tags.type().toString())
                .toLowerCase();
    }
}
