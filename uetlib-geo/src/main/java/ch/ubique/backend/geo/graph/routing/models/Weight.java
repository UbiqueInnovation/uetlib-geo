package ch.ubique.backend.geo.graph.routing.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record Weight(float weight) {

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("weight", weight)
                .toString();
    }
}
