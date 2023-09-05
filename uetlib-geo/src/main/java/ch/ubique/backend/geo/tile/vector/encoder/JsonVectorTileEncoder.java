package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pivovarit.function.exception.WrappedException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.jackson.datatype.jts.JtsModule;

public class JsonVectorTileEncoder extends AbstractVectorTileEncoder {

    protected final ObjectMapper objectMapper;

    public JsonVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        final var srid = config.tileLevelsDefinition().origin().srid();
        final var geometryFactory = new GeometryFactory(new PrecisionModel(1), srid);

        this.objectMapper =
                config.objectMapper()
                        .copy()
                        .registerModule(new JtsModule(geometryFactory))
                        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public final byte[] encodeVectorTile(final VectorTile tile) {
        try {
            return objectMapper.writeValueAsBytes(mapToOutputObject(tile));
        } catch (final JsonProcessingException e) {
            throw new WrappedException(e);
        }
    }

    protected Object mapToOutputObject(final VectorTile tile) {
        return tile;
    }
}
