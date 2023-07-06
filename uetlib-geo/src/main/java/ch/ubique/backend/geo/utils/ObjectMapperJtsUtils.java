package ch.ubique.backend.geo.utils;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import org.locationtech.jts.geom.GeometryFactory;
import org.n52.jackson.datatype.jts.JtsModule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ObjectMapperJtsUtils {

    private ObjectMapperJtsUtils() {}

    public static ObjectMapper objectMapperWithJts(final GeometryFactory geometryFactory) {
        final var javaTimeModule = new JavaTimeModule();
        final var localDateSerializer =
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        javaTimeModule.addSerializer(LocalDate.class, localDateSerializer);

        return JsonMapper.builder()
                .addModule(javaTimeModule)
                .addModule(new JtsModule(geometryFactory))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    public static ObjectMapper objectMapperLv95() {
        return objectMapperWithJts(JtsConverter.LV95.geometryFactory());
    }

    public static ObjectMapper objectMapperLv03() {
        return objectMapperWithJts(JtsConverter.LV03.geometryFactory());
    }

    public static ObjectMapper objectMapperWgs84() {
        return objectMapperWithJts(JtsConverter.WGS84.geometryFactory());
    }

    public static ObjectMapper objectMapperWebMercator() {
        return objectMapperWithJts(JtsConverter.WEB_MERCATOR.geometryFactory());
    }
}
