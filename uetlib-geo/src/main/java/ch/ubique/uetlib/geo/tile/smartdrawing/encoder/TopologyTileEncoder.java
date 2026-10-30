package ch.ubique.uetlib.geo.tile.smartdrawing.encoder;

import static com.google.common.primitives.Ints.asList;

import ch.ubique.uetlib.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.uetlib.geo.tile.smartdrawing.models.SmartDrawingVertex;
import ch.ubique.uetlib.geo.tile.smartdrawing.protobuf.TopologyTile;
import ch.ubique.uetlib.geo.tile.smartdrawing.protobuf.TopologyTile.Edge;
import ch.ubique.uetlib.geo.tile.smartdrawing.protobuf.TopologyTile.Edge.Builder;
import ch.ubique.uetlib.geo.tile.smartdrawing.utils.SmartDrawingUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jspecify.annotations.Nullable;

public class TopologyTileEncoder {

    private int weightEpsilon;

    private int extent = 4096;
    private boolean encodeElevation = true;

    private final ReferencedEnvelope tileEnvelope;

    private final List<SmartDrawingEdge> edges;

    public TopologyTileEncoder(final ReferencedEnvelope tileEnvelope) {
        this.tileEnvelope = tileEnvelope;
        this.edges = new ArrayList<>();
    }

    public TopologyTileEncoder extent(final int extent) {
        this.extent = extent;
        return this;
    }

    public TopologyTileEncoder encodeElevation(final boolean encodeElevation) {
        this.encodeElevation = encodeElevation;
        return this;
    }

    /** maximum allowed encoding error for weights */
    public TopologyTileEncoder weightEpsilon(final int weightEpsilon) {
        this.weightEpsilon = weightEpsilon;
        return this;
    }

    public TopologyTileEncoder add(final SmartDrawingEdge edge) {
        edges.add(edge);
        return this;
    }

    public TopologyTileEncoder addAll(final Collection<SmartDrawingEdge> edges) {
        this.edges.addAll(edges);
        return this;
    }

    public TopologyTile encode() {

        final var sortedVertices = sortedVertices();

        return TopologyTile.newBuilder()
                .setExtent(extent)
                .setHasElevation(encodeElevation)
                .addAllEncodedVertices(encodedVertices(sortedVertices))
                .addAllEdges(buildEdges(sortedVertices))
                .build();
    }

    private List<Long> encodedVertices(final List<SmartDrawingVertex> vertices) {
        if (vertices.isEmpty()) {
            return List.of();
        }

        final var coordinateClass = vertices.getFirst().coordinate().getClass();

        final var numComponents = encodeElevation ? 4 : 3;
        final var encoded = new ArrayList<Long>(vertices.size() * numComponents);
        final var coordinateEncoder = new TileCoordinateEncoder(extent, tileEnvelope);

        var lastId = 0L;
        var lastX = 0L;
        var lastY = 0L;
        var lastZ = 0L;

        for (final var vertex : vertices) {
            if (!vertex.coordinate().getClass().equals(coordinateClass)) {
                throw new IllegalArgumentException("All coordinates must be of the same class");
            }
            final var id = vertex.id();
            final var worldCoordinate = vertex.coordinate().toJts();
            final var tileCoordinate = coordinateEncoder.computeTileCoordinate(worldCoordinate);
            final var x = tileCoordinate.x();
            final var y = tileCoordinate.y();
            encoded.add(id - lastId);
            encoded.add(x - lastX);
            encoded.add(y - lastY);
            lastId = id;
            lastX = x;
            lastY = y;
            if (encodeElevation) {
                final var z = tileCoordinate.requireZ();
                encoded.add(z - lastZ);
                lastZ = z;
            }
        }
        return encoded;
    }

    private List<SmartDrawingVertex> sortedVertices() {
        return edges.stream()
                .flatMap(e -> Stream.of(e.u(), e.v()))
                .sorted(Comparator.comparingLong(SmartDrawingVertex::id))
                .distinct()
                .toList();
    }

    private Collection<TopologyTile.Edge> buildEdges(
            final List<SmartDrawingVertex> sortedVertices) {
        final var indexOfVertices = HashMap.<Long, Integer>newHashMap(sortedVertices.size());
        for (var i = 0; i < sortedVertices.size(); i++) {
            indexOfVertices.put(sortedVertices.get(i).id(), i);
        }
        final var canonicalEdges =
                edges.stream().map(SmartDrawingEdge::canonicalDirection).sorted().toList();

        validateDuplicateEdges(canonicalEdges);

        return canonicalEdges.stream()
                .map(edge -> buildEdge(edge, edge.multiEdgeVariant(), indexOfVertices))
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
            final @Nullable Long multiEdgeVariant,
            final HashMap<Long, Integer> indexOfVertices) {
        final var edgeBuilder =
                Edge.newBuilder()
                        .setUIdx(Objects.requireNonNull(indexOfVertices.get(edge.u().id())))
                        .setVIdx(Objects.requireNonNull(indexOfVertices.get(edge.v().id())));
        setWeights(edge, edgeBuilder);
        if (multiEdgeVariant != null) {
            edgeBuilder.setMultiEdgeVariant(multiEdgeVariant);
        }
        if (edge.isHighway().length > 0) {
            edgeBuilder.setIsHighway(SmartDrawingUtil.booleanArrayToInt(edge.isHighway()));
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
