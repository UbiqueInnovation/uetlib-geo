package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingVertex;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Map;

class TopologyTileEncoderTest {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testEncodeEmpty() {
        final var encoder = new TopologyTileEncoder();
        final var tile = encoder.encode();
        assertThat(tile.getVerticesCount()).isZero();
        assertThat(tile.getEdgesCount()).isZero();
    }

    @Test
    void testEncodeSingleEdge() {
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
                        new int[] {10},
                        new int[] {10},
                        null,
                        false);

        final var tile = new TopologyTileEncoder().add(edge).encode();

        assertThat(tile.getVerticesCount()).isEqualTo(2);
        assertThat(tile.getVertices(0).getId()).isEqualTo(1);
        assertThat(tile.getVertices(0).getX()).isEqualTo(8.0f);
        assertThat(tile.getVertices(0).getY()).isEqualTo(47.0f);
        assertThat(tile.getVertices(1).getId()).isEqualTo(2);
        assertThat(tile.getVertices(1).getX()).isEqualTo(8.1f);
        assertThat(tile.getVertices(1).getY()).isEqualTo(47.1f);

        assertThat(tile.getEdgesCount()).isEqualTo(1);
        final var encodedEdge = tile.getEdges(0);
        assertThat(encodedEdge.getU()).isEqualTo(1);
        assertThat(encodedEdge.getV()).isEqualTo(2);
        assertThat(encodedEdge.getWeightsList()).containsExactly(10);
        assertThat(encodedEdge.getForwardsWeightsCount()).isZero();
        assertThat(encodedEdge.getBackwardsWeightsCount()).isZero();
    }

    @Test
    void testEncodeVertexElevation() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0, 123.456));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(47.1, 8.1));
        final var edge = createEdge(v1, v2);

        final var tile = new TopologyTileEncoder().add(edge).encode();

        assertThat(tile.getVerticesCount()).isEqualTo(2);
        assertThat(tile.getVertices(0).getElevation()).isEqualTo(12346); // 123.456 * 100 rounded
        assertThat(tile.getVertices(1).getElevation())
                .isZero(); // Default value for int32 in proto if not set
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
                new int[] {},
                new int[] {},
                null,
                false);
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
                        false);

        final var tile = new TopologyTileEncoder().add(edge).encode();

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
                        false);
        final var tile1 = new TopologyTileEncoder().add(edge1).encode();
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
                        false);
        final var tile2 = new TopologyTileEncoder().add(edge2).encode();
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
                        false);

        // Without epsilon (default 0)
        final var tile1 = new TopologyTileEncoder().add(edge).encode();
        assertThat(tile1.getEdges(0).getWeightsCount()).isZero();
        assertThat(tile1.getEdges(0).getForwardsWeightsList()).containsExactly(100);

        // With epsilon 5
        final var tile2 = new TopologyTileEncoder().weightEpsilon(5).add(edge).encode();
        assertThat(tile2.getEdges(0).getWeightsList()).containsExactly(100);
        assertThat(tile2.getEdges(0).getForwardsWeightsCount()).isZero();

        // With epsilon 4
        final var tile3 = new TopologyTileEncoder().weightEpsilon(4).add(edge).encode();
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
                        false);

        final var tile = new TopologyTileEncoder().add(edge).encode();

        assertThat(tile.getEdgesCount()).isEqualTo(1);
        final var encodedEdge = tile.getEdges(0);
        assertThat(encodedEdge.getU()).isEqualTo(1);
        assertThat(encodedEdge.getV()).isEqualTo(2);
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

        final var tile = new TopologyTileEncoder().add(List.of(edge1, edge2)).encode();

        assertThat(tile.getVerticesCount()).isEqualTo(3);
        assertThat(tile.getVertices(0).getId()).isEqualTo(1);
        assertThat(tile.getVertices(1).getId()).isEqualTo(2);
        assertThat(tile.getVertices(2).getId()).isEqualTo(3);
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
                        new int[] {},
                        new int[] {},
                        123L,
                        true);

        final var tile = new TopologyTileEncoder().add(edge).encode();

        assertThat(tile.getEdges(0).getMultiEdgeVariant()).isEqualTo(123L);
        assertThat(tile.getEdges(0).getIsHighway()).isTrue();
    }

    @Test
    void testMismatchedWeightLengths() {
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
                        new int[] {10},
                        new int[] {20, 30},
                        null,
                        false);

        final var encoder = new TopologyTileEncoder().add(edge);
        assertThatThrownBy(encoder::encode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forward and backward weights must have the same length");
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
                        false);
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
                        false);

        final var tile = new TopologyTileEncoder().add(edge1).add(edge2).encode();

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
                        false);
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
                        false);

        final var encoder = new TopologyTileEncoder().add(edge1).add(edge2);

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
                        false);
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
                        false);

        final var encoder = new TopologyTileEncoder().add(edge1).add(edge2);

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

        final var tile = new TopologyTileEncoder().add(edge32).add(edge13).add(edge12).encode();

        assertThat(tile.getEdgesCount()).isEqualTo(3);
        // Sorted by (u, v): (1, 2), (1, 3), (2, 3)
        assertThat(tile.getEdges(0).getU()).isEqualTo(1);
        assertThat(tile.getEdges(0).getV()).isEqualTo(2);

        assertThat(tile.getEdges(1).getU()).isEqualTo(1);
        assertThat(tile.getEdges(1).getV()).isEqualTo(3);

        assertThat(tile.getEdges(2).getU()).isEqualTo(2);
        assertThat(tile.getEdges(2).getV()).isEqualTo(3);
    }

    @Test
    void testMismatchedCoordinateClasses() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(47.0, 8.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(48.0, 8.0));
        final var l1 = new SmartDrawingVertex(3, new LV95Coordinate(2600000, 1200000));
        final var l2 = new SmartDrawingVertex(4, new LV95Coordinate(2600000, 1300000));

        final var encoder =
                new TopologyTileEncoder().add(createEdge(v1, v2)).add(createEdge(l1, l2));
        assertThatThrownBy(encoder::encode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All coordinates must be of the same class");
    }
}
