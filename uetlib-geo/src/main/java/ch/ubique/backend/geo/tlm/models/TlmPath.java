package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.locationtech.jts.geom.LineString;

public record TlmPath(@NotNull LineString line, @NotNull @Valid TlmPathInfo info) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final TlmPath tlmPath)) {
            return false;
        }

        return new EqualsBuilder().append(info, tlmPath.info).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(info).toHashCode();
    }
}
