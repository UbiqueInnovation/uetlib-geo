package ch.ubique.backend.geo.jackson;

import ch.ubique.backend.core.jackson.JsonMapperUtils;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.jackson.jts.JtsModule;

import org.locationtech.jts.geom.GeometryFactory;

import tools.jackson.databind.json.JsonMapper;

public final class JsonMapperJtsUtils {

    private JsonMapperJtsUtils() {}

    public static void configureJts(
            final JsonMapper.Builder jsonMapperBuilder, final GeometryFactory geometryFactory) {
        jsonMapperBuilder.addModule(new JtsModule(geometryFactory));
    }

    public static void configureJtsLv95(final JsonMapper.Builder jsonMapperBuilder) {
        configureJts(jsonMapperBuilder, CoordinateSystems.LV95.geometryFactory());
    }

    public static void configureJtsLv03(final JsonMapper.Builder jsonMapperBuilder) {
        configureJts(jsonMapperBuilder, CoordinateSystems.LV03.geometryFactory());
    }

    public static void configureJtsWgs84(final JsonMapper.Builder jsonMapperBuilder) {
        configureJts(jsonMapperBuilder, CoordinateSystems.WGS84.geometryFactory());
    }

    public static void configureJtsWebMercator(final JsonMapper.Builder jsonMapperBuilder) {
        configureJts(jsonMapperBuilder, CoordinateSystems.WEB_MERCATOR.geometryFactory());
    }

    /**
     * Prefer to not use this method directly, use the Spring Boot autoconfigured one, together with
     * the autoconfiguration from {@code uetlib-spring-boot}, together with a {@code
     * UetlibJacksonGeoJtsLv95JsonMapperBuilderCustomizer} bean from {@code uetlib-spring-boot-geo}.
     * This one exists for cases where the other one is super inconvenient.
     */
    public static JsonMapper jsonMapperLv95() {
        final var jsonMapperBuilder = JsonMapper.builder();
        JsonMapperUtils.configureBase(jsonMapperBuilder);
        configureJtsLv95(jsonMapperBuilder);
        return jsonMapperBuilder.build();
    }

    /**
     * Prefer to not use this method directly, use the Spring Boot autoconfigured one, together with
     * the autoconfiguration from {@code uetlib-spring-boot}, together with a {@code
     * UetlibJacksonGeoJtsLv03JsonMapperBuilderCustomizer} bean from {@code uetlib-spring-boot-geo}.
     * This one exists for cases where the other one is super inconvenient.
     */
    public static JsonMapper jsonMapperLv03() {
        final var jsonMapperBuilder = JsonMapper.builder();
        JsonMapperUtils.configureBase(jsonMapperBuilder);
        configureJtsLv03(jsonMapperBuilder);
        return jsonMapperBuilder.build();
    }

    /**
     * Prefer to not use this method directly, use the Spring Boot autoconfigured one, together with
     * the autoconfiguration from {@code uetlib-spring-boot}, together with a {@code
     * UetlibJacksonGeoJtsWgs84JsonMapperBuilderCustomizer} bean from {@code
     * uetlib-spring-boot-geo}. This one exists for cases where the other one is super inconvenient.
     */
    public static JsonMapper jsonMapperWgs84() {
        final var jsonMapperBuilder = JsonMapper.builder();
        JsonMapperUtils.configureBase(jsonMapperBuilder);
        configureJtsWgs84(jsonMapperBuilder);
        return jsonMapperBuilder.build();
    }

    /**
     * Prefer to not use this method directly, use the Spring Boot autoconfigured one, together with
     * the autoconfiguration from {@code uetlib-spring-boot}, together with a {@code
     * UetlibJacksonGeoJtsWebMercatorJsonMapperBuilderCustomizer} bean from {@code
     * uetlib-spring-boot-geo}. This one exists for cases where the other one is super inconvenient.
     */
    public static JsonMapper jsonMapperWebMercator() {
        final var jsonMapperBuilder = JsonMapper.builder();
        JsonMapperUtils.configureBase(jsonMapperBuilder);
        configureJtsWebMercator(jsonMapperBuilder);
        return jsonMapperBuilder.build();
    }
}
