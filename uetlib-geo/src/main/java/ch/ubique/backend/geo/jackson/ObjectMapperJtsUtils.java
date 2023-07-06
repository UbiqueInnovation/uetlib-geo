package ch.ubique.backend.geo.jackson;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.locationtech.jts.geom.GeometryFactory;
import org.n52.jackson.datatype.jts.JtsModule;

public final class ObjectMapperJtsUtils {

    private ObjectMapperJtsUtils() {}

    public static ObjectMapper objectMapperWithJts(final GeometryFactory geometryFactory) {
        return ObjectMapperUtils.objectMapperBuilder()
                .addModule(new JtsModule(geometryFactory))
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
