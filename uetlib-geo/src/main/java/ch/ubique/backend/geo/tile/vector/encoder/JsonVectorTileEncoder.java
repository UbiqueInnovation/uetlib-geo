package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;

import tools.jackson.databind.ObjectMapper;
import com.pivovarit.function.exception.WrappedException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.jackson.datatype.jts.JtsModule;
import tools.jackson.core.JacksonException;

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
        try {
            return objectMapper.writeValueAsBytes(mapToOutputObject(tile));
        } catch (final JacksonException e) {
            throw new WrappedException(e);
        }
    }

    protected Object mapToOutputObject(final VectorTile tile) {
        return tile;
    }
}
