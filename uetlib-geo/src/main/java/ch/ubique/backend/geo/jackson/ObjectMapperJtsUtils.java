package ch.ubique.backend.geo.jackson;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

import ch.ubique.backend.geo.jackson.jts.JtsModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.locationtech.jts.geom.GeometryFactory;

import java.util.function.Consumer;

public final class ObjectMapperJtsUtils {

    private ObjectMapperJtsUtils() {}

    public static ObjectMapper objectMapperWithJts(final GeometryFactory geometryFactory) {
        return objectMapperWithJts(geometryFactory, ignore -> {});
    }

    /** Pass a consumer to customize the default object mapper */
    public static ObjectMapper objectMapperWithJts(
            final GeometryFactory geometryFactory, final Consumer<JsonMapper.Builder> customizer) {
        return ObjectMapperUtils.objectMapperBuilder(customizer)
                .addModule(new JtsModule(geometryFactory))
                .build();
    }

    public static ObjectMapper objectMapperLv95() {
        return objectMapperWithJts(CoordinateSystems.LV95.geometryFactory());
    }

    public static ObjectMapper objectMapperLv95(final Consumer<JsonMapper.Builder> customizer) {
        return objectMapperWithJts(CoordinateSystems.LV95.geometryFactory(), customizer);
    }

    public static ObjectMapper objectMapperLv03() {
        return objectMapperWithJts(CoordinateSystems.LV03.geometryFactory());
    }

    public static ObjectMapper objectMapperLv03(final Consumer<JsonMapper.Builder> customizer) {
        return objectMapperWithJts(CoordinateSystems.LV03.geometryFactory(), customizer);
    }

    public static ObjectMapper objectMapperWgs84() {
        return objectMapperWithJts(CoordinateSystems.WGS84.geometryFactory());
    }

    public static ObjectMapper objectMapperWgs84(final Consumer<JsonMapper.Builder> customizer) {
        return objectMapperWithJts(CoordinateSystems.WGS84.geometryFactory(), customizer);
    }

    public static ObjectMapper objectMapperWebMercator() {
        return objectMapperWithJts(CoordinateSystems.WEB_MERCATOR.geometryFactory());
    }

    public static ObjectMapper objectMapperWebMercator(
            final Consumer<JsonMapper.Builder> customizer) {
        return objectMapperWithJts(CoordinateSystems.WEB_MERCATOR.geometryFactory(), customizer);
    }
}
