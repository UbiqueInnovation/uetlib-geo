package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementUtils;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;

import java.util.Optional;
import java.util.function.Function;

public final class GeoJsonVectorTileEncoder extends JsonVectorTileEncoder {

    public GeoJsonVectorTileEncoder(final VectorTileConfig config) {
        super(config);
    }

    @Override
    protected Object mapToOutputObject(final VectorTile tile) {
        final var features =
                tile.elements().stream()
                        .map(toFeature(tile.tileInfo().envelope()))
                        .flatMap(Optional::stream)
                        .toList();

        return new FeatureCollection().setFeatures(features);
    }

    private Function<VectorTileElement, Optional<Feature>> toFeature(
            final ReferencedEnvelope bounds) {
        return e -> getTileGeometry(e, bounds).map(toFeature(e));
    }

    private Function<Geometry, Feature> toFeature(final VectorTileElement element) {
        return geometry ->
                new Feature()
                        .setType("Feature")
                        .setGeometry(geometry)
                        .setProperties(VectorTileElementUtils.getFinalTags(element, getConfig()))
                        .setId(String.valueOf(element.id()));
    }
}
