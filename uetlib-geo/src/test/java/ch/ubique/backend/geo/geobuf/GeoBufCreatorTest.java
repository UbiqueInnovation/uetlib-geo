package ch.ubique.backend.geo.geobuf;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WGS84;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.backend.core.file.temp.TempFileManager;
import ch.ubique.backend.core.process.ProcessConfig;
import ch.ubique.backend.core.utils.ProcessUtils;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeatureCollection;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GeoBufCreatorTest {
    private static final Logger logger = LoggerFactory.getLogger(GeoBufCreatorTest.class);
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
            fromGeoBufNode(pbf, actualJson);
            final var actual = objectMapper.readValue(actualJson.toFile(), FeatureCollection.class);
            assertEquals(features.getFeatures(), actual.getFeatures());
        }
    }

    public static void fromGeoBufNode(@NotNull final Path geobuf, @NotNull final Path outputGeojson)
            throws IOException {
        final var result =
                ProcessUtils.executeProcess(
                        ProcessConfig.builder()
                                .command(
                                        List.of(
                                                "bash",
                                                "-c",
                                                String.format(
                                                        "geobuf2json < %s > %s",
                                                        geobuf.toAbsolutePath(),
                                                        outputGeojson.toAbsolutePath())))
                                .timeout(Duration.ofSeconds(4))
                                .build());
        if (result.didFinish() && result.success()) {
            return;
        }
        logger.info("geobuf2json failed not completed: {}", result);
        throw new IOException("Could not convert " + geobuf + " to " + outputGeojson + ".");
    }

    /**
     * Requires `json2geobuf` to be installed (install it via npm), and be available on the command
     * line.
     */
    public static @NotNull byte[] toGeoBufNode(
            @NotNull final GenericFeatureCollection<?, ?, ?> featureCollection) throws IOException {
        try (final var tempFileManager = new TempFileManager()) {
            final var geojsonFile =
                    tempFileManager.createTempFile(
                            objectMapper.writeValueAsBytes(featureCollection), ".geojson");
            final var tempFilePath = tempFileManager.createEmptyTempFile(".pbf");
            toGeoBufNode(geojsonFile, tempFilePath);
            try (final var stream = Files.newInputStream(tempFilePath)) {
                return stream.readAllBytes();
            }
        }
    }

    /**
     * Requires `json2geobuf` to be installed (install it via npm), and be available on the command
     * line.
     */
    public static void toGeoBufNode(@NotNull final Path geojson, @NotNull final Path outputPbf)
            throws IOException {
        logger.debug("Starting converting {} to {}", geojson, outputPbf);
        final var result =
                ProcessUtils.executeProcess(
                        ProcessConfig.builder()
                                .command(
                                        List.of(
                                                "bash", // Command with redirect
                                                "-c",
                                                String.format(
                                                        "json2geobuf < %s > %s",
                                                        geojson.toAbsolutePath(),
                                                        outputPbf.toAbsolutePath())))
                                .timeout(Duration.ofSeconds(4))
                                .build());
        if (result.didFinish() && result.success()) {
            return;
        }
        logger.info("json2geobuf failed not completed: {}", result);
        throw new IOException("Could not convert " + geojson + " to " + outputPbf + ".");
    }
}
