package ch.ubique.backend.geo.tile.smartdrawing.models;

import org.apache.commons.lang3.compare.ComparableUtils;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.LineString;

import java.util.Map;
import java.util.Objects;

public record SmartDrawingEdge(
        SmartDrawingVertex u,
        SmartDrawingVertex v,
        LineString geometry,
        Map<String, Object> tags,
        int[] forwardWeights,
        int[] backwardWeights,
        @Nullable Long multiEdgeVariant,
        boolean[] isHighway)
        implements Comparable<SmartDrawingEdge> {

    public SmartDrawingEdge {
        final var geometrySrid = geometry.getSRID();
        if (u.coordinate().srid() != geometrySrid || v.coordinate().srid() != geometrySrid) {
            throw new IllegalArgumentException("Vertex SRID does not match geometry SRID");
        }
        if (!geometry.getCoordinateN(0).equals2D(u.coordinate().toJts())) {
            throw new IllegalArgumentException(
                    "First coordinate of geometry does not match start vertex");
        }
        if (!geometry.getCoordinateN(geometry.getNumPoints() - 1)
                .equals2D(v.coordinate().toJts())) {
            throw new IllegalArgumentException(
                    "Last coordinate of geometry does not match end vertex");
        }

        final var numDisciplines =
                ComparableUtils.max(forwardWeights.length, backwardWeights.length);

        if (numDisciplines == 0) {
            throw new IllegalArgumentException("No weights for disciplines provided");
        }
        if (forwardWeights.length > 0 && forwardWeights.length != numDisciplines) {
            throw new IllegalArgumentException(
                    "forwardWeights length " + forwardWeights.length + " does not match numDisciplines "
                            + numDisciplines);
        }
        if (backwardWeights.length > 0 && backwardWeights.length != numDisciplines) {
            throw new IllegalArgumentException(
                    "backwardWeights length " + backwardWeights.length + " does not match numDisciplines "
                            + numDisciplines);
        }
        if (isHighway.length > 0 && isHighway.length != numDisciplines) {
            throw new IllegalArgumentException(
                    "isHighway length " + isHighway.length + " does not match numDisciplines " + numDisciplines);
        }
    }

    public SmartDrawingEdge canonicalDirection() {
        if (u.id() <= v.id()) {
            return this;
        }
        return new SmartDrawingEdge(
                v,
                u,
                geometry.reverse(),
                tags,
                backwardWeights,
                forwardWeights,
                multiEdgeVariant,
                isHighway);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj
                || (obj instanceof final SmartDrawingEdge e
                        && e.u.equals(u)
                        && e.v.equals(v)
                        && Objects.equals(multiEdgeVariant, e.multiEdgeVariant));
    }

    @Override
    public int hashCode() {
        var result = u.hashCode();
        result = 31 * result + v.hashCode();
        result = 31 * result + Objects.hashCode(multiEdgeVariant);
        return result;
    }

    @Override
    public String toString() {
        if (multiEdgeVariant != null) {
            return "(" + u + ", " + v + ", " + multiEdgeVariant + ")";
        }
        return "(" + u + ", " + v + ")";
    }

    @Override
    public int compareTo(final SmartDrawingEdge o) {
        final var compareU = Long.compare(u.id(), o.u.id());
        if (compareU != 0) {
            return compareU;
        }
        return Long.compare(v.id(), o.v.id());
    }
}
