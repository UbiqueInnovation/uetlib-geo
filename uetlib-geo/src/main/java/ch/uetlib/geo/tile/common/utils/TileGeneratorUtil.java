package ch.uetlib.geo.tile.common.utils;

import ch.uetlib.geo.tile.vector.models.TileOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public final class TileGeneratorUtil {

    private static final Logger logger = LoggerFactory.getLogger(TileGeneratorUtil.class);

    private TileGeneratorUtil() {}

    public static void generateTiles(final Stream<? extends TileOutput> outputs)
            throws ExecutionException, InterruptedException {
        final var iterator = outputs.iterator();

        if (!iterator.hasNext()) {
            throw new NoSuchElementException("Got no outputs.");
        }

        logger.info("Processing each output.");
        while (iterator.hasNext()) {

            final var output = iterator.next();
            logger.info("Got output {}.", output);

            logger.info("Generating and uploading tiles");
            final var tileGenerator = TileGeneratorFactory.getGenerator(output);
            tileGenerator.generateTiles();
        }
    }
}
