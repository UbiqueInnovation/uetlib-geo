package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static com.google.common.primitives.Ints.asList;

import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingVertex;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.TopologyTile;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.TopologyTile.Edge;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.TopologyTile.Edge.Builder;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.TopologyTile.Vertex;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TopologyTileEncoder {

    private int weightEpsilon;
    private final Collection<SmartDrawingEdge> edges;

    public TopologyTileEncoder() {
        edges = new ArrayList<>();
    }

    /** weight differences smaller than this are considered equal */
    public TopologyTileEncoder weightEpsilon(final int weightEpsilon) {
        this.weightEpsilon = weightEpsilon;
        return this;
    }

    public TopologyTileEncoder add(final SmartDrawingEdge edge) {
        edges.add(edge);
        return this;
    }

    public TopologyTileEncoder add(final Collection<SmartDrawingEdge> edges) {
        this.edges.addAll(edges);
        return this;
    }

    public TopologyTile encode() {
        return TopologyTile.newBuilder()
                .addAllVertices(buildVertices())
                .addAllEdges(buildEdges())
                .build();
    }

    private List<Vertex> buildVertices() {
        if (edges.isEmpty()) {
            return List.of();
        }

        final var coordinateClass = edges.iterator().next().u().coordinate().getClass();

        return edges.stream()
                .flatMap(e -> Stream.of(e.u(), e.v()))
                .sorted(Comparator.comparingLong(SmartDrawingVertex::id))
                .distinct()
                .map(v -> buildVertex(v, coordinateClass))
                .toList();
    }

    private TopologyTile.Vertex buildVertex(
            final SmartDrawingVertex vertex, final Class<?> expectedCoordinateClass) {
        if (!vertex.coordinate().getClass().equals(expectedCoordinateClass)) {
            throw new IllegalArgumentException("All coordinates must be of the same class");
        }
        final var coordinate = vertex.coordinate().toJts();
        final var vertexBuilder =
                Vertex.newBuilder()
                        .setId(vertex.id())
                        .setX((float) coordinate.x)
                        .setY((float) coordinate.y);
        final var elevation = coordinate.getZ();
        if (elevation != Coordinate.NULL_ORDINATE) {
            final var elevationCentimeters = (int) Math.round(elevation * 100);
            vertexBuilder.setElevation(elevationCentimeters);
        }
        return vertexBuilder.build();
    }

    private Collection<TopologyTile.Edge> buildEdges() {
        final var canonicalEdges =
                edges.stream().map(SmartDrawingEdge::canonicalDirection).sorted().toList();

        validateDuplicateEdges(canonicalEdges);

        return canonicalEdges.stream()
                .map(edge -> buildEdge(edge, edge.multiEdgeVariant()))
                .toList();
    }

    private void validateDuplicateEdges(final List<SmartDrawingEdge> sortedEdges) {
        if (sortedEdges.isEmpty()) {
            return;
        }

        var i = 0;
        while (i < sortedEdges.size()) {
            final var edge = sortedEdges.get(i);
            final var u = edge.u().id();
            final var v = edge.v().id();
            var j = i + 1;
            while (j < sortedEdges.size()
                    && sortedEdges.get(j).u().id() == u
                    && sortedEdges.get(j).v().id() == v) {
                j++;
            }

            final var groupSize = j - i;
            if (groupSize > 1) {
                final var variants = new java.util.HashSet<Long>();
                for (var k = i; k < j; k++) {
                    final var variant = sortedEdges.get(k).multiEdgeVariant();
                    if (variant == null || !variants.add(variant)) {
                        throw new IllegalArgumentException(
                                "Multiple edges between the same vertices must have distinct multiEdgeVariants");
                    }
                }
            }
            i = j;
        }
    }

    private TopologyTile.Edge buildEdge(
            final SmartDrawingEdge edge,
            final @org.jspecify.annotations.Nullable Long multiEdgeVariant) {
        final var edgeBuilder = Edge.newBuilder().setU(edge.u().id()).setV(edge.v().id());
        setWeights(edge, edgeBuilder);
        if (multiEdgeVariant != null) {
            edgeBuilder.setMultiEdgeVariant(multiEdgeVariant);
        }
        if (edge.isHighway()) {
            edgeBuilder.setIsHighway(true);
        }
        return edgeBuilder.build();
    }

    private void setWeights(final SmartDrawingEdge smartDrawingEdge, final Builder edgeBuilder) {
        final var forwards = smartDrawingEdge.forwardWeights();
        final var backwards = smartDrawingEdge.backwardWeights();

        if (forwards.length == 0 || backwards.length == 0) {
            if (forwards.length > 0) {
                edgeBuilder.addAllForwardsWeights(asList(forwards));
            } else if (backwards.length > 0) {
                edgeBuilder.addAllBackwardsWeights(asList(backwards));
            }
            return;
        }

        // Both directions have weights
        if (areWeightsSymmetric(forwards, backwards)) {
            edgeBuilder.addAllWeights(asList(forwards));
        } else {
            edgeBuilder.addAllForwardsWeights(asList(forwards));
            edgeBuilder.addAllBackwardsWeights(asList(backwards));
        }
    }

    private boolean areWeightsSymmetric(final int[] forwards, final int[] backwards) {
        final var numWeights = forwards.length;
        if (backwards.length != numWeights) {
            throw new IllegalArgumentException(
                    "Forward and backward weights must have the same length");
        }

        for (var i = 0; i < numWeights; i++) {
            if (Math.abs(forwards[i] - backwards[i]) > weightEpsilon) {
                return false;
            }
        }
        return true;
    }
}
