package ch.ubique.backend.geo.utils;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.LV95;
import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WGS84;
import static ch.ubique.backend.geo.jackson.JsonMapperJtsUtils.jsonMapperLv95;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class TestGeoJsonUtil {

    public static final Logger logger = LoggerFactory.getLogger(TestGeoJsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = jsonMapperLv95();

    private TestGeoJsonUtil() {}

    private static final Path TEMP_FOLDER = Path.of("/tmp/uetlib-geo/test");

    public static void saveGeoJson(final Collection<Feature> features, final String fileName) {
        try {
            final var file = TEMP_FOLDER.resolve(fileName + ".geojson").toFile();
            FileUtils.createParentDirectories(file);
            OBJECT_MAPPER.writeValue(file, new FeatureCollection().setFeatures(features));
            logger.info("Wrote debug output: {}", file);
        } catch (IOException e) {
            logger.warn("Could not write debug output.", e);
        }
    }

    public static Feature toFeature(final List<? extends Vertex<RoutingVertex, ?>> path) {
        final var geometry =
                WGS84.geometryFactory()
                        .createLineString(
                                path.stream()
                                        .map(Vertex::getProperties)
                                        .map(RoutingVertex::coordinate)
                                        .map(c -> JtsConverter.WGS84.transform(c, LV95.srid()))
                                        .toArray(Coordinate[]::new));

        return new Feature().setGeometry(geometry).setType("Feature");
    }

    public static Stream<Feature> routingGraphToFeatures(final RoutingGraphs graphs) {
        return Stream.concat(
                graphs.routingGraph().getVertices().stream().map(TestGeoJsonUtil::toFeature),
                graphs.routingGraph().getDirectedEdges().stream().map(TestGeoJsonUtil::toFeature));
    }

    public static Feature toFeature(final Vertex<RoutingVertex, ?> vertex) {
        final var geometry =
                JtsConverter.WGS84.transform(
                        LV95.geometryFactory().createPoint(vertex.getProperties().coordinate()));
        return new Feature()
                .setType("Feature")
                .setGeometry(geometry)
                .setProperties(Map.of("id", String.valueOf(vertex.getId())));
    }

    public static Feature toFeature(final Edge<RoutingVertex, RoutingShortcutEdge> edge) {
        final var geometry =
                JtsConverter.WGS84.transform(
                        LV95.geometryFactory()
                                .createLineString(
                                        new Coordinate[] {
                                            edge.getU().getProperties().coordinate(),
                                            edge.getV().getProperties().coordinate()
                                        }));
        return new Feature()
                .setType("Feature")
                .setGeometry(geometry)
                .setProperties(Map.of("direction", edge.getProperties().direction().getId()));
    }
}
