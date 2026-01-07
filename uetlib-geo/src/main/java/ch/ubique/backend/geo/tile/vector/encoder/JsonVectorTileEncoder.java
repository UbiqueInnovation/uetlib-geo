package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.jackson.jts.JtsModule;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import tools.jackson.databind.ObjectMapper;

public class JsonVectorTileEncoder extends AbstractVectorTileEncoder {

    protected final ObjectMapper objectMapper;

    public JsonVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        final var srid = config.tileLevelsDefinition().origin().srid();
        final var geometryFactory = new GeometryFactory(new PrecisionModel(1), srid);

        this.objectMapper =
                config.objectMapper().rebuild().addModule(new JtsModule(geometryFactory)).build();
    }

    @Override
    public final byte[] encodeVectorTile(final VectorTile tile) {
        return objectMapper.writeValueAsBytes(mapToOutputObject(tile));
    }

    protected Object mapToOutputObject(final VectorTile tile) {
        return tile;
    }
}
