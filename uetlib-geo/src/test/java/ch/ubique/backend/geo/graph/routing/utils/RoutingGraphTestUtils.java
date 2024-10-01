package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.n52.jackson.datatype.jts.JtsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public final class RoutingGraphTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(RoutingGraphTestUtils.class);

    private static final GeometryFactory geometryFactory = CoordinateSystems.LV95.geometryFactory();
    private static final ObjectMapper objectMapper =
            JsonMapper.builder().addModule(new JtsModule(geometryFactory)).build();

    public static void dumpAsGeoJson(final RoutingGraphs graphs) throws IOException {
        final var routingVertices =
                graphs.routingGraph().getVertices().stream().map(RoutingGraphTestUtils::toGeometry);

        final var routingEdges =
                graphs.routingGraph().getDirectedEdges().stream()
                        .map(RoutingGraphTestUtils::toGeometry);

        dumpAsGeoJson(Stream.concat(routingEdges, routingVertices).toList());
    }

    public static void dumpAsGeoJson(final Collection<? extends Geometry> geometries)
            throws IOException {
        final var featureCollection =
                new FeatureCollection()
                        .setFeatures(
                                geometries.stream()
                                        .map(
                                                geometry ->
                                                        new Feature()
                                                                .setGeometry(geometry)
                                                                .setType("Feature")
                                                                .setProperties(Map.of()))
                                        .toList());

        final var tempFile = Files.createTempFile(null, ".geojson");
        objectMapper.writeValue(tempFile.toFile(), featureCollection);

        logger.info("Dumped to geojson: {}", tempFile);
    }

    private static Geometry toGeometry(
            final Vertex<
                            RoutingGraphVertexProperties,
                            WeightAndDirection<RoutingGraphVertexProperties, Weight>>
                    vertex) {
        return geometryFactory.createPoint(vertex.getProperties().coordinate());
    }

    private static Geometry toGeometry(
            final Edge<
                            RoutingGraphVertexProperties,
                            WeightAndDirection<RoutingGraphVertexProperties, Weight>>
                    edge) {
        return geometryFactory.createLineString(
                new Coordinate[] {
                    edge.getU().getProperties().coordinate(),
                    edge.getV().getProperties().coordinate()
                });
    }
}
