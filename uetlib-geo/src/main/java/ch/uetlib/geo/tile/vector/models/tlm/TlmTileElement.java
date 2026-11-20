package ch.uetlib.geo.tile.vector.models.tlm;

import ch.uetlib.geo.tile.vector.models.VectorTileElementWithTags;
import ch.uetlib.geo.tlm.models.TlmPath;

import org.locationtech.jts.geom.Geometry;

import java.util.Locale;
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
                .toLowerCase(Locale.ROOT);
    }
}
