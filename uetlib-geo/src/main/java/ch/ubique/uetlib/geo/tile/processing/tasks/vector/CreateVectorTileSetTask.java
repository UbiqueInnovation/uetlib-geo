package ch.ubique.uetlib.geo.tile.processing.tasks.vector;

import ch.ubique.uetlib.geo.tile.common.models.TileInfo;
import ch.ubique.uetlib.geo.tile.common.models.TileOutputFormat;
import ch.ubique.uetlib.geo.tile.processing.tasks.CreateTileSetTask;
import ch.ubique.uetlib.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.ubique.uetlib.geo.tile.vector.mapbox.models.VectorTile;
import ch.ubique.uetlib.geo.tile.vector.mapbox.optimization.VectorTileOptimizer;
import ch.ubique.uetlib.geo.tile.vector.mapbox.tilejson.TileJson;
import ch.ubique.uetlib.geo.tile.vector.mapbox.tilejson.builder.TileJsonBuilder;
import ch.ubique.uetlib.geo.tile.vector.models.BinaryTile;
import java.io.IOException;

public abstract class CreateVectorTileSetTask extends CreateTileSetTask {

    private final TileJsonBuilder tileJsonBuilder;

    protected CreateVectorTileSetTask() {
        this.tileJsonBuilder = TileJson.builder();
    }

    protected abstract VectorTile createVectorTile(final TileInfo tileInfo) throws IOException;

    protected abstract void saveTileJson(final TileJson tileJson) throws IOException;

    protected TileJsonBuilder customizeTileJson(final TileJsonBuilder tileJsonBuilder) {
        return tileJsonBuilder;
    }

    @Override
    protected final BinaryTile createTile(final TileInfo tileInfo) throws IOException {
        final var vectorTile = createVectorTile(tileInfo);
        final var optimized = VectorTileOptimizer.optimize(vectorTile);
        recordInfoForTileJson(tileInfo, optimized);
        final var encoded = VectorTileEncoder.encode(optimized);
        return new BinaryTile(tileInfo, TileOutputFormat.PBF, encoded.toByteArray());
    }

    @Override
    public void onCompletion() throws IOException {
        final var tileJson = customizeTileJson(tileJsonBuilder).build();
        saveTileJson(tileJson);
        super.onCompletion();
    }

    private void recordInfoForTileJson(final TileInfo tileInfo, final VectorTile vectorTile) {
        final var level = tileInfo.level();
        tileJsonBuilder.addLevel(level);
        for (final var layer : vectorTile.layers()) {
            final var layerBuilder = tileJsonBuilder.vectorLayerBuilder(layer.name());
            layerBuilder.addLevel(level);
            for (final var feature : layer.features()) {
                for (final var tag : feature.tags().entrySet()) {
                    layerBuilder.addField(tag.getKey(), tag.getValue());
                }
            }
        }
    }
}
