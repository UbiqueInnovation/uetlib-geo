package ch.ubique.uetlib.geo.tile.vector.encoder;

import ch.ubique.uetlib.geo.jackson.jts.JtsModule;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTile;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileConfig;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.core.JacksonException;
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

    /**
     * @throws JacksonException if encoding with Jackson JsonMapper fails
     */
    @Override
    public final byte[] encodeVectorTile(final VectorTile tile) {
        return objectMapper.writeValueAsBytes(mapToOutputObject(tile));
    }

    protected Object mapToOutputObject(final VectorTile tile) {
        return tile;
    }
}
