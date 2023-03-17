package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;
import ch.ubique.backend.geo.tlm.models.TlmPath;

import org.locationtech.jts.geom.Geometry;

import java.util.Objects;
import java.util.Optional;

public record TlmTileElement(
        long id,
        Geometry geometry,
        TlmInfo tags
) implements VectorTileElementWithTags<TlmInfo> {

    private static long lastId  ;

    public TlmTileElement(final Geometry geometry, final TlmInfo tags) {
        this(lastId++, geometry, tags);
    }

    public static TlmTileElement fromTlmPath(final TlmPath tlmPath) {
        return new TlmTileElement(
                tlmPath.getLine(),
                new TlmInfo(tlmPath));
    }

    @Override
    public String layer() {
        return Optional
                .ofNullable(tags.trailType()).map(Objects::toString)
                .orElse(tags.type().toString())
                .toLowerCase();
    }
}
