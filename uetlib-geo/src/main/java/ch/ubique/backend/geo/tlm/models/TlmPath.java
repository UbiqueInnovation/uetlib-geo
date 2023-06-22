package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.LineString;

public record TlmPath(@NotNull LineString line, @NotNull @Valid TlmPathInfo info) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var path = (TlmPath) o;
        return info.equals(path.info);
    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }
}
