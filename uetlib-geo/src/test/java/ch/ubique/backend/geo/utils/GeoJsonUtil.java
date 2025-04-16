package ch.ubique.backend.geo.utils;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.LV95;
import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WGS84;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class GeoJsonUtil {
    public static final Logger logger = LoggerFactory.getLogger(GeoJsonUtil.class);

    private GeoJsonUtil() {}

    private static final Path TEMP_FOLDER = Path.of("/tmp/uetlib-geo/test");

    public static void saveGeoJson(final Collection<Feature> features, final String fileName) {
        try {
            final var file = TEMP_FOLDER.resolve(fileName + ".geojson").toFile();
            FileUtils.createParentDirectories(file);
            ObjectMapperJtsUtils.objectMapperWgs84()
                    .writeValue(file, new FeatureCollection().setFeatures(features));
            logger.info("Wrote debug output: {}", file);
        } catch (IOException e) {
            logger.warn("Could not write debug output.", e);
        }
    }

    public static Feature toFeature(
            final List<? extends Vertex<RoutingGraphVertexProperties, ?>> path) {
        final var geometry =
                WGS84.geometryFactory()
                        .createLineString(
                                path.stream()
                                        .map(Vertex::getProperties)
                                        .map(RoutingGraphVertexProperties::coordinate)
                                        .map(c -> JtsConverter.WGS84.transform(c, LV95.srid()))
                                        .toArray(Coordinate[]::new));

        return new Feature().setGeometry(geometry).setType("Feature");
    }
}
