package ch.ubique.backend.geo.geojson.parser;

import ch.ubique.backend.geo.geojson.models.FeatureCollection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @deprecated This implementation does not use JTS geometries. Since it requires an object mapper
 *     based on the callers needs, this class will not be replaced. Use
 *     `objectMapper.readValue(input, FeatureCollection.class)` instead. Will be removed with 0.9.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
public class GeoJsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GeoJsonParser() {}

    public static FeatureCollection parse(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, FeatureCollection.class);
    }

    public static FeatureCollection parse(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, FeatureCollection.class);
    }

    public static FeatureCollection parse(URL url) throws IOException {
        return objectMapper.readValue(url, FeatureCollection.class);
    }

    public static FeatureCollection parse(String content) throws IOException {
        return objectMapper.readValue(content, FeatureCollection.class);
    }
}
