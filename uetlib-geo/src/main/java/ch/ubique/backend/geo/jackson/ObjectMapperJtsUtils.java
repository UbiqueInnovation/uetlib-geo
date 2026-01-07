package ch.ubique.backend.geo.jackson;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.jackson.jts.JtsModule;

import org.locationtech.jts.geom.GeometryFactory;

import tools.jackson.databind.json.JsonMapper;

import java.util.function.Consumer;

public final class ObjectMapperJtsUtils {

    private ObjectMapperJtsUtils() {}

    public static JsonMapper jsonMapperWithJts(final GeometryFactory geometryFactory) {
        return jsonMapperWithJts(geometryFactory, ignore -> {});
    }

    /** Pass a consumer to customize the default object mapper */
    public static JsonMapper jsonMapperWithJts(
            final GeometryFactory geometryFactory, final Consumer<JsonMapper.Builder> customizer) {
        return ObjectMapperUtils.jsonMapperBuilder(customizer)
                .addModule(new JtsModule(geometryFactory))
                .build();
    }

    public static JsonMapper jsonMapperLv95() {
        return jsonMapperWithJts(CoordinateSystems.LV95.geometryFactory());
    }

    public static JsonMapper jsonMapperLv95(final Consumer<JsonMapper.Builder> customizer) {
        return jsonMapperWithJts(CoordinateSystems.LV95.geometryFactory(), customizer);
    }

    public static JsonMapper jsonMapperLv03() {
        return jsonMapperWithJts(CoordinateSystems.LV03.geometryFactory());
    }

    public static JsonMapper jsonMapperLv03(final Consumer<JsonMapper.Builder> customizer) {
        return jsonMapperWithJts(CoordinateSystems.LV03.geometryFactory(), customizer);
    }

    public static JsonMapper jsonMapperWgs84() {
        return jsonMapperWithJts(CoordinateSystems.WGS84.geometryFactory());
    }

    public static JsonMapper jsonMapperWgs84(final Consumer<JsonMapper.Builder> customizer) {
        return jsonMapperWithJts(CoordinateSystems.WGS84.geometryFactory(), customizer);
    }

    public static JsonMapper jsonMapperWebMercator() {
        return jsonMapperWithJts(CoordinateSystems.WEB_MERCATOR.geometryFactory());
    }

    public static JsonMapper jsonMapperWebMercator(final Consumer<JsonMapper.Builder> customizer) {
        return jsonMapperWithJts(CoordinateSystems.WEB_MERCATOR.geometryFactory(), customizer);
    }
}
