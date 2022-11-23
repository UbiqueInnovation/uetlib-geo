package ch.ubique.backend.geo.graph.routing.utils;

import com.pivovarit.function.ThrowingFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;

import java.util.stream.Stream;


class LinesToRoutingGraphUtilTest {

    @Test
    void test() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines = Stream.of(
                        "LINESTRING (50 0, 50 49.5)",
                        "LINESTRING (0 50, 100 50)"
                )
                .map(ThrowingFunction.unchecked(wktReader::read))
                .map(LineString.class::cast)
                .map(line -> new LineWithInfo<>(line, testInfo))
                .toList();

        final var graph = LinesToRoutingGraphUtil.buildGraphWithInfo(lines);

        Assertions.assertNotNull(graph);
        Assertions.assertEquals(4, graph.getVertices().size());
        Assertions.assertEquals(3, graph.getEdges().size());
        Assertions.assertTrue(graph.getEdges().stream().allMatch(e -> testInfo.equals(e.getProperties())));
    }

    @Test
    void testDuplicateLines() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines = Stream.of(
                        "LINESTRING (0.1 0, 100 99.4)",
                        "LINESTRING (0 0.3, 100 100.2)",
                        "LINESTRING (0.4 0, 100 100.4)",
                        "LINESTRING (0 0.6, 100.2 100)"
                )
                .map(ThrowingFunction.unchecked(wktReader::read))
                .map(LineString.class::cast)
                .map(line -> new LineWithInfo<>(line, testInfo))
                .toList();

        final var graph = LinesToRoutingGraphUtil.buildGraphWithInfo(lines);

        Assertions.assertNotNull(graph);
        Assertions.assertEquals(2, graph.getVertices().size());
        Assertions.assertEquals(1, graph.getEdges().size());
        Assertions.assertTrue(graph.getEdges().stream().allMatch(e -> testInfo.equals(e.getProperties())));
    }
}
