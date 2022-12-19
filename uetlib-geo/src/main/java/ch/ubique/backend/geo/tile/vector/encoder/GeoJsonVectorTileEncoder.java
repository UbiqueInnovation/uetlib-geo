package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pivovarit.function.exception.WrappedException;
import java.util.Optional;
import java.util.function.Function;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.jackson.datatype.jts.JtsModule;

public final class GeoJsonVectorTileEncoder extends AbstractVectorTileEncoder {

    private final ObjectMapper objectMapper;

    public GeoJsonVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        final var srid = config.getTileLevelsDefinition().origin().srid();
        final var geometryFactory = new GeometryFactory(new PrecisionModel(1), srid);

        this.objectMapper =
                JsonMapper.builder()
                        .addModule(new JavaTimeModule())
                        .addModule(new JtsModule(geometryFactory))
                        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                        .build();
    }

    @Override
    public byte[] encodeVectorTile(final VectorTile tile) {
        final var features =
                tile.elements().stream()
                        .map(toFeature(tile.tileInfo().envelope()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

        try {
            return objectMapper.writeValueAsBytes(new FeatureCollection().setFeatures(features));
        } catch (final JsonProcessingException e) {
            throw new WrappedException(e);
        }
    }

    private Function<VectorTileElement, Optional<Feature>> toFeature(
            final ReferencedEnvelope bounds) {
        return e -> getTileGeometry(e, bounds).map(toFeature(e));
    }

    private static Function<Geometry, Feature> toFeature(final VectorTileElement element) {
        return geometry ->
                new Feature()
                        .setType("Feature")
                        .setGeometry(geometry)
                        .setProperties(VectorTileElementUtils.getFinalTags(element))
                        .setId(String.valueOf(element.id()));
    }
}
