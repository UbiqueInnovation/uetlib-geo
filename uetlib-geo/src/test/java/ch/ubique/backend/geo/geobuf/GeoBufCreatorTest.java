package ch.ubique.backend.geo.geobuf;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WGS84;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.backend.core.file.temp.TempFileManager;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.locationtech.jts.geom.Coordinate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GeoBufCreatorTest {
    private static final ObjectMapper objectMapper = ObjectMapperJtsUtils.objectMapperWgs84();

    @Test
    @EnabledIf("ch.ubique.backend.geo.geobuf.GeoBufCreator#isGeoBufNodeAvailable")
    void testCreateNative() throws IOException {
        try (final var tempFileManager = new TempFileManager()) {
            final var creator = new GeoBufCreator();
            final var features =
                    new FeatureCollection()
                            .setFeatures(
                                    List.of(
                                            new Feature()
                                                    .setType("Feature")
                                                    .setId("1")
                                                    .setGeometry(
                                                            WGS84.geometryFactory()
                                                                    .createLineString(
                                                                            new Coordinate[] {
                                                                                new Coordinate(
                                                                                        0, 0),
                                                                                new Coordinate(
                                                                                        1, 1),
                                                                                new Coordinate(
                                                                                        1, 0),
                                                                                new Coordinate(0, 0)
                                                                            }))
                                                    .setProperties(
                                                            Map.of(
                                                                    "some_property",
                                                                    "some_value"))));
            final var bytes = creator.toGeoBufNative(features);
            final var pbf = tempFileManager.createTempPath("-test.pbf");
            try (final var os = new FileOutputStream(pbf.toFile())) {
                os.write(bytes);
            }
            final var actualJson = tempFileManager.createTempPath("-test.geojson");
            GeoBufCreator.fromGeoBufNode(pbf, actualJson);
            final var actual = objectMapper.readValue(actualJson.toFile(), FeatureCollection.class);
            assertEquals(features.getFeatures(), actual.getFeatures());
        }
    }
}
