package ch.ubique.backend.geo.graph.algorithms;

import static ch.ubique.backend.geo.graph.routing.utils.RoutingGraphTest.createGraphsFromGeoJson;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.MapCollectors;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.dijkstra.SearchVertex;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.utils.GeoJsonUtil;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

class DijkstraTest {

    @Test
    void simpleTest() {
        final var graph = new Graph<Void, Double>();
        final var start = graph.createVertex(null);
        final var a = graph.createVertex(null);
        final var b = graph.createVertex(null);
        final var target = graph.createVertex(null);

        // start -> a -> target
        graph.addDirectedEdge(start, a, 1.0);
        graph.addDirectedEdge(a, target, 6.0);

        // start -> b -> target
        graph.addDirectedEdge(start, b, 2.0);
        graph.addDirectedEdge(b, target, 4.0);

        final var dijkstra = new Dijkstra<Void, Double>(Edge::getProperties).addStartVertex(start);

        final var v1 = dijkstra.findNext();
        assertEquals(new SearchVertex<>(start, 0, null), v1);

        final var v2 = dijkstra.findNext();
        assertEquals(new SearchVertex<>(a, 1.0, v1), v2);

        final var v3 = dijkstra.findNext();
        assertEquals(new SearchVertex<>(b, 2.0, v1), v3);

        final var v4 = dijkstra.findNext();
        assertEquals(new SearchVertex<>(target, 6.0, v3), v4);

        assertNull(dijkstra.findNext());
        assertNull(dijkstra.findNext());

        assertEquals(1.0, new Dijkstra<>(Edge::getProperties, start).find(a).score());
        assertEquals(2.0, new Dijkstra<>(Edge::getProperties, start).find(b).score());
        assertEquals(6.0, new Dijkstra<>(Edge::getProperties, start).find(target).score());
    }

    @Test
    void testExample() throws IOException {
        final var graphs = createGraphsFromGeoJson("/alp_la_schera.geojson");
        final var completeGraph = graphs.completeGraph();
        final var a = closestVertex(completeGraph, new LV95Coordinate(2808526, 1171427).toJts());
        final var b = closestVertex(completeGraph, new LV95Coordinate(2812273, 1169019).toJts());
        final var c = closestVertex(completeGraph, new LV95Coordinate(2811755, 1171791).toJts());
        final var d = closestVertex(completeGraph, new LV95Coordinate(2811272, 1167343).toJts());
        final var e = closestVertex(completeGraph, new LV95Coordinate(2809429, 1170888).toJts());
        final var f = closestVertex(completeGraph, new LV95Coordinate(2811205, 1168696).toJts());

        final var results =
                new Dijkstra<>(edge -> edge.getProperties().weight(), a)
                        .find(List.of(b, c, d, e, f));
        assertNotNull(results);
        assertEquals(5, results.size());
        GeoJsonUtil.saveGeoJson(
                results.stream().map(SearchUtils::computePath).map(GeoJsonUtil::toFeature).toList(),
                "dijkstra-test");

        final var resultsByTarget =
                results.stream().collect(MapCollectors.toMap(SearchVertex::vertex));

        final var resultAB = resultsByTarget.get(b);
        final var pathAB = SearchUtils.computePath(resultAB);
        GeoJsonUtil.saveGeoJson(List.of(GeoJsonUtil.toFeature(pathAB)), "path-a-b");
        assertEquals(983, pathAB.size());
        assertEquals(8_829, resultAB.score(), 1);

        final var resultAC = resultsByTarget.get(c);
        final var pathAC = SearchUtils.computePath(resultAC);
        assertEquals(1139, pathAC.size());
        assertEquals(10_304, resultAC.score(), 1);
    }

    private <E> Vertex<RoutingGraphVertexProperties, E> closestVertex(
            final Graph<RoutingGraphVertexProperties, E> graph, final Coordinate coordinate) {
        return graph.getVertices().stream()
                .min(Comparator.comparing(v -> v.getProperties().coordinate().distance(coordinate)))
                .orElseThrow();
    }
}
