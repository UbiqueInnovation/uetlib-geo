package ch.ubique.backend.geo.tile.processing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;
import ch.ubique.backend.geo.tile.common.io.TileLoader;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.processing.tasks.ModifyTileSetTask;
import ch.ubique.backend.geo.tile.processing.tasks.TileSetTask;
import ch.ubique.backend.geo.tile.vector.mapbox.decoding.VectorTileDecoder;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.ubique.backend.geo.tile.vector.mapbox.optimization.VectorTileOptimizer;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

import com.pivovarit.function.ThrowingRunnable;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.IntStream;

class TileSetProcessorTest {

    static TileLoader swisstopoTileLoader;

    @BeforeAll
    static void setup() throws IOException {
        swisstopoTileLoader =
                TileLoader.fromTileJsonUrl(
                        "https://vectortiles.geo.admin.ch/tiles/ch.swisstopo.base.vt/v1.0.0/tiles.json");
    }

    @Test
    void testThrows() {
        try (final var generator = new TileSetProcessor(4)) {
            assertThrows(
                    InterruptedException.class,
                    () -> generator.executeTask(failingTask(new InterruptedException())));
            assertThrows(
                    IOException.class, () -> generator.executeTask(failingTask(new IOException())));
            final var notImplementedFailure = failingTask(new NotImplementedException());
            assertThrows(
                    NotImplementedException.class,
                    () -> generator.executeTask(notImplementedFailure));
            final var oomFailure =
                    taskFromRunnable(
                            () -> {
                                throw new OutOfMemoryError();
                            });
            assertThrows(CompletionException.class, () -> generator.executeTask(oomFailure));
        }
    }

    @Test
    void replicateSwisstopo() {
        final var task = new TestTask();
        try (final var generator = new TileSetProcessor(4)) {
            assertDoesNotThrow(() -> generator.executeTask(task));
        }
    }

    @Test
    void optimizeSwisstopo() throws IOException {
        final var task =
                new TestTask() {
                    @Override
                    public BinaryTile modify(final BinaryTile tile) throws IOException {
                        final var decoded = VectorTileDecoder.decode(tile.bytes());
                        final var optimized = VectorTileOptimizer.optimize(decoded);
                        final var encoded = VectorTileEncoder.encode(optimized);
                        final var result =
                                new BinaryTile(
                                        tile.info(), TileOutputFormat.PBF, encoded.toByteArray());
                        assertThat(result.bytes()).hasSizeLessThanOrEqualTo(tile.bytes().length);
                        return result;
                    }
                };
        try (final var generator = new TileSetProcessor(4)) {
            generator.executeTask(task);
        }
    }

    private static class TestTask extends ModifyTileSetTask {

        @Override
        public Collection<TileInfo> selectedTiles() {
            final var bounds =
                    new BoundingBox<>(
                            new WGS84Coordinate(45.818, 5.9559),
                            new WGS84Coordinate(47.8084, 10.4921));
            final var envelope = JtsConverter.WEB_MERCATOR.transform(bounds).toJts();
            return WebMercatorTileDefinitions.tileDefinitions()
                    .tileInfos(IntStream.rangeClosed(0, 8), envelope)
                    .toList();
        }

        @Override
        public BinaryTile loadTile(final TileInfo tileInfo) throws IOException {
            return swisstopoTileLoader.loadPbfTile(tileInfo);
        }

        @Override
        public BinaryTile modify(final BinaryTile tile) throws IOException {
            return tile;
        }

        @Override
        public void saveTile(final BinaryTile tile) {
            // nothing to do here
            assertNotNull(tile);
        }
    }

    private static <E extends Exception> TileSetTask<Exception> failingTask(final E exception) {
        return taskFromRunnable(
                () -> {
                    throw exception;
                });
    }

    private static <E extends Exception> TileSetTask<E> taskFromRunnable(
            final ThrowingRunnable<E> runnable) {
        return new TileSetTask<>() {
            @Override
            public Collection<TileInfo> selectedTiles() {
                return List.of(Mockito.mock(TileInfo.class));
            }

            @Override
            public void execute(final TileInfo tileInfo) throws E {
                runnable.run();
            }
        };
    }
}
