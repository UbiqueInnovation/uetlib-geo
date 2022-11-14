package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import org.locationtech.jts.geom.Geometry;

import java.util.Objects;
import java.util.Optional;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.LV95;

public record TlmTileElement(
        long id,
        Geometry geometry,
        TlmInfo tags
) implements VectorTileElementWithTags<TlmInfo> {

    @Override
    public String layer() {
        return Optional
                .ofNullable(tags.trailType()).map(Objects::toString)
                .orElse(tags.type().toString())
                .toLowerCase();
    }

    private static long lastId = 0;

    public TlmTileElement(Geometry geometry, TlmInfo tags) {
        this(lastId++, geometry, tags);
    }

    public static TlmTileElement fromTlmPath(final TlmPath tlmPath) {
        return new TlmTileElement(
                LV95.toJtsLineString(tlmPath.getPolyline()),
                new TlmInfo(tlmPath));
    }
}
