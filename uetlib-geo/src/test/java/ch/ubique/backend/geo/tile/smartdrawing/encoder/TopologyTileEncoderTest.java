package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WEB_MERCATOR;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingVertex;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.TopologyTile;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Map;

class TopologyTileEncoderTest {

    private static final ReferencedEnvelope TILE_ENVELOPE =
            new ReferencedEnvelope(0, 10, 0, 10, WEB_MERCATOR.crs());
    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testEncodeEmpty() {
        final var encoder =
                new TopologyTileEncoder(new ReferencedEnvelope(0, 1, 0, 1, WEB_MERCATOR.crs()));
        final var tile = encoder.encode();
        assertThat(tile.getEncodedVerticesCount()).isZero();
        assertThat(tile.getEdgesCount()).isZero();
    }

    @Test
    void testEncodeSingleEdge() {
        final var v1 = new SmartDrawingVertex(-3, new WGS84Coordinate(7.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(7.3, 8.1));
        final var edge =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {10},
                        null,
                        new boolean[0]);

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge).encode();
        final var extent = tile.getExtent();

        assertThat(tile.getEncodedVerticesCount()).isEqualTo(2 * 3);
        final var encoded = tile.getEncodedVerticesList();
        assertThat(encoded.get(0)).isEqualTo(-3); // id 1
        assertThat(encoded.get(1)).isEqualTo(Math.round(8.0 / 10 * extent)); // x
        assertThat(encoded.get(2)).isEqualTo(Math.round((10 - 7.0) / 10 * extent)); // y

        assertThat(encoded.get(3)).isEqualTo(5); // id 2 - id -3
        assertThat(encoded.get(4)).isEqualTo(Math.round(0.1 / 10 * extent)); // dx
        assertThat(encoded.get(5)).isEqualTo(Math.round(-0.3 / 10 * extent)); // dx

        final var vertexIds = getVertexIds(tile);

        assertThat(tile.getEdgesCount()).isEqualTo(1);
        final var encodedEdge = tile.getEdges(0);
        assertThat(vertexIds[encodedEdge.getUIdx()]).isEqualTo(-3);
        assertThat(vertexIds[encodedEdge.getVIdx()]).isEqualTo(2);
        assertThat(encodedEdge.getWeightsList()).containsExactly(10);
        assertThat(encodedEdge.getForwardsWeightsCount()).isZero();
        assertThat(encodedEdge.getBackwardsWeightsCount()).isZero();
    }

    private static long @NonNull [] getVertexIds(@UnknownNullability final TopologyTile tile) {
        final var numVertices = tile.getEncodedVerticesCount() / (tile.getHasElevation() ? 4 : 3);
        final var vertexIds = new long[numVertices];
        for (var i = 0; i < numVertices; i++) {
            final var prev = i > 0 ? vertexIds[i - 1] : 0;
            vertexIds[i] = prev + tile.getEncodedVertices(i * 3);
        }
        return vertexIds;
    }

    @Test
    void testEncodeVertexElevation() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0, 123.456));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1, 0.0));
        final var edge = createEdge(v1, v2);

        final var tile = new TopologyTileEncoder(TILE_ENVELOPE).add(edge).encode();

        assertThat(tile.getEncodedVerticesCount()).isEqualTo(2 * 4);
        final var encoded = tile.getEncodedVerticesList();
        assertThat(encoded.get(3)).isEqualTo(12346); // v1 elevation
        assertThat(encoded.get(7)).isEqualTo(-12346); // v2 elevation - v1 elevation
    }

    private static @NonNull SmartDrawingEdge createEdge(
            final SmartDrawingVertex u, final SmartDrawingVertex v) {
        return new SmartDrawingEdge(
                u,
                v,
                u.coordinate()
                        .toJtsPoint()
                        .getFactory()
                        .createLineString(
                                new Coordinate[] {u.coordinate().toJts(), v.coordinate().toJts()}),
                Map.of(),
                new int[] {0},
                new int[] {0},
                null,
                new boolean[0]);
    }

    @Test
    void testEncodeAsymmetricWeights() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var edge =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10, 20},
                        new int[] {15, 25},
                        null,
                        new boolean[0]);

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge).encode();

        assertThat(tile.getEdgesCount()).isEqualTo(1);
        final var encodedEdge = tile.getEdges(0);
        assertThat(encodedEdge.getWeightsCount()).isZero();
        assertThat(encodedEdge.getForwardsWeightsList()).containsExactly(10, 20);
        assertThat(encodedEdge.getBackwardsWeightsList()).containsExactly(15, 25);
    }

    @Test
    void testEncodeOneSidedWeights() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));

        // Forwards only
        final var edge1 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {},
                        null,
                        new boolean[0]);
        final var tile1 =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge1).encode();
        assertThat(tile1.getEdges(0).getForwardsWeightsList()).containsExactly(10);
        assertThat(tile1.getEdges(0).getBackwardsWeightsCount()).isZero();
        assertThat(tile1.getEdges(0).getWeightsCount()).isZero();

        // Backwards only
        final var edge2 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {},
                        new int[] {20},
                        null,
                        new boolean[0]);
        final var tile2 =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge2).encode();
        assertThat(tile2.getEdges(0).getBackwardsWeightsList()).containsExactly(20);
        assertThat(tile2.getEdges(0).getForwardsWeightsCount()).isZero();
        assertThat(tile2.getEdges(0).getWeightsCount()).isZero();
    }

    @Test
    void testWeightEpsilon() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var edge =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {100},
                        new int[] {105},
                        null,
                        new boolean[0]);

        // Without epsilon (default 0)
        final var tile1 =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge).encode();
        assertThat(tile1.getEdges(0).getWeightsCount()).isZero();
        assertThat(tile1.getEdges(0).getForwardsWeightsList()).containsExactly(100);

        // With epsilon 5
        final var tile2 =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .weightEpsilon(5)
                        .add(edge)
                        .encode();
        assertThat(tile2.getEdges(0).getWeightsList()).containsExactly(100);
        assertThat(tile2.getEdges(0).getForwardsWeightsCount()).isZero();

        // With epsilon 4
        final var tile3 =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .weightEpsilon(4)
                        .add(edge)
                        .encode();
        assertThat(tile3.getEdges(0).getWeightsCount()).isZero();
        assertThat(tile3.getEdges(0).getForwardsWeightsList()).containsExactly(100);
    }

    @Test
    void testCanonicalDirection() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));

        // Edge from 2 to 1 (should be flipped)
        final var edge =
                new SmartDrawingEdge(
                        v2,
                        v1,
                        GF.createLineString(
                                new Coordinate[] {
                                    v2.coordinate().toJts(), v1.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {20},
                        null,
                        new boolean[0]);

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge).encode();

        final var vertexIds = getVertexIds(tile);

        assertThat(tile.getEdgesCount()).isEqualTo(1);
        final var encodedEdge = tile.getEdges(0);
        assertThat(vertexIds[encodedEdge.getUIdx()]).isEqualTo(1);
        assertThat(vertexIds[encodedEdge.getVIdx()]).isEqualTo(2);
        // Weights should be flipped as well: forwards was 10 (2->1), now it's backwards for 1->2
        assertThat(encodedEdge.getForwardsWeightsList()).containsExactly(20);
        assertThat(encodedEdge.getBackwardsWeightsList()).containsExactly(10);
    }

    @Test
    void testVertexDeduplicationAndSorting() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var v3 = new SmartDrawingVertex(3, new WGS84Coordinate(47.2, 8.2));

        final var edge1 = createEdge(v2, v1);
        final var edge2 = createEdge(v2, v3);

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .addAll(List.of(edge1, edge2))
                        .encode();

        assertThat(tile.getEncodedVerticesCount()).isEqualTo(3 * 3);
        final var encoded = tile.getEncodedVerticesList();
        assertThat(encoded.get(0)).isEqualTo(1);
        assertThat(encoded.get(3)).isEqualTo(1); // id 2 - id 1
        assertThat(encoded.get(6)).isEqualTo(1); // id 3 - id 2
    }

    @Test
    void testFlags() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var edge =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {0},
                        new int[] {0},
                        123L,
                        new boolean[] {true});

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge).encode();

        assertThat(tile.getEdges(0).getMultiEdgeVariant()).isEqualTo(123L);
        assertThat(tile.getEdges(0).getIsHighway()).isEqualTo(1);
    }

    @Test
    void testMismatchedWeightLengths() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        assertThatThrownBy(
                        () ->
                                new SmartDrawingEdge(
                                        v1,
                                        v2,
                                        GF.createLineString(
                                                new Coordinate[] {
                                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                                }),
                                        Map.of(),
                                        new int[] {10},
                                        new int[] {20, 30},
                                        null,
                                        new boolean[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("forwardWeights length 1 does not match numDisciplines 2");
    }

    @Test
    void testMultipleEdgesWithDistinctVariantsPass() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));

        final var edge1 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {10},
                        1L,
                        new boolean[0]);
        final var edge2 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {20},
                        new int[] {20},
                        2L,
                        new boolean[0]);

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .add(edge1)
                        .add(edge2)
                        .encode();

        assertThat(tile.getEdgesCount()).isEqualTo(2);
        assertThat(tile.getEdges(0).getMultiEdgeVariant()).isEqualTo(1L);
        assertThat(tile.getEdges(1).getMultiEdgeVariant()).isEqualTo(2L);
    }

    @Test
    void testMultipleEdgesWithDuplicateVariantsFail() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));

        final var edge1 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {10},
                        1L,
                        new boolean[0]);
        final var edge2 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {20},
                        new int[] {20},
                        1L,
                        new boolean[0]);

        final var encoder =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge1).add(edge2);

        assertThatThrownBy(encoder::encode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Multiple edges between the same vertices must have distinct multiEdgeVariants");
    }

    @Test
    void testMultipleEdgesWithMissingVariantsFail() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));

        final var edge1 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {10},
                        new int[] {10},
                        null,
                        new boolean[0]);
        final var edge2 =
                new SmartDrawingEdge(
                        v1,
                        v2,
                        GF.createLineString(
                                new Coordinate[] {
                                    v1.coordinate().toJts(), v2.coordinate().toJts()
                                }),
                        Map.of(),
                        new int[] {20},
                        new int[] {20},
                        null,
                        new boolean[0]);

        final var encoder =
                new TopologyTileEncoder(TILE_ENVELOPE).encodeElevation(false).add(edge1).add(edge2);

        assertThatThrownBy(encoder::encode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Multiple edges between the same vertices must have distinct multiEdgeVariants");
    }

    @Test
    void testEdgeSorting() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var v3 = new SmartDrawingVertex(3, new WGS84Coordinate(47.2, 8.2));

        // Added in reverse/unsorted order
        final var edge32 = createEdge(v3, v2); // Canonical: (2, 3)
        final var edge13 = createEdge(v1, v3); // Canonical: (1, 3)
        final var edge12 = createEdge(v1, v2); // Canonical: (1, 2)

        final var tile =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .add(edge32)
                        .add(edge13)
                        .add(edge12)
                        .encode();

        assertThat(tile.getEdgesCount()).isEqualTo(3);

        final var vertexIds = getVertexIds(tile);
        // Sorted by (u, v): (1, 2), (1, 3), (2, 3)
        assertThat(vertexIds[tile.getEdges(0).getUIdx()]).isEqualTo(1);
        assertThat(vertexIds[tile.getEdges(0).getVIdx()]).isEqualTo(2);

        assertThat(vertexIds[tile.getEdges(1).getUIdx()]).isEqualTo(1);
        assertThat(vertexIds[tile.getEdges(1).getVIdx()]).isEqualTo(3);

        assertThat(vertexIds[tile.getEdges(2).getUIdx()]).isEqualTo(2);
        assertThat(vertexIds[tile.getEdges(2).getVIdx()]).isEqualTo(3);
    }

    @Test
    void testMismatchedCoordinateClasses() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(48.0, 8.0));
        final var l1 = new SmartDrawingVertex(3, new LV95Coordinate(2600000, 1200000));
        final var l2 = new SmartDrawingVertex(4, new LV95Coordinate(2600000, 1300000));

        final var encoder =
                new TopologyTileEncoder(TILE_ENVELOPE)
                        .encodeElevation(false)
                        .add(createEdge(v1, v2))
                        .add(createEdge(l1, l2));
        assertThatThrownBy(encoder::encode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All coordinates must be of the same class");
    }
}
