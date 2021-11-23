package ch.ubique.backend.geo.geojson.parser;

import ch.ubique.backend.geo.geojson.models.FeatureCollection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GeoJsonParser {

    private final static ObjectMapper objectMapper = new ObjectMapper();

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
