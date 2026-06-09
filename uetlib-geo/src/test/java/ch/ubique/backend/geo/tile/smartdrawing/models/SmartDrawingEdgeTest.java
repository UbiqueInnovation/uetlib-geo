package ch.ubique.backend.geo.tile.smartdrawing.models;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Map;

class SmartDrawingEdgeTest {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testConstructorArrayLengths() {
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(0.0, 0.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(10.0, 10.0));
        final var line = GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(10, 10)});
        final Map<String, Object> tags = Map.of();

        // All zero length - should fail
        assertThatThrownBy(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[0], new int[0], null, new boolean[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No weights for disciplines provided");

        // Same non-zero length - should pass
        assertThatCode(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[2], new int[2], null, new boolean[2]))
                .doesNotThrowAnyException();

        // Mixed zero and same non-zero length - should pass
        assertThatCode(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[2], new int[0], null, new boolean[2]))
                .doesNotThrowAnyException();

        // Mismatched non-zero lengths - should fail
        assertThatThrownBy(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[2], new int[1], null, new boolean[2]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("backwardWeights length 1 does not match numDisciplines 2");

        assertThatThrownBy(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[2], new int[2], null, new boolean[1]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("isHighway length 1 does not match numDisciplines 2");

        assertThatThrownBy(() -> new SmartDrawingEdge(v1, v2, line, tags, new int[1], new int[2], null, new boolean[2]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("forwardWeights length 1 does not match numDisciplines 2");
    }
}
