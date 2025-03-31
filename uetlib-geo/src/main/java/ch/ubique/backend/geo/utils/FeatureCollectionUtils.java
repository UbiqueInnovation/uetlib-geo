package ch.ubique.backend.geo.utils;

import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeatureCollection;

import java.util.function.Function;

public class FeatureCollectionUtils {
    private FeatureCollectionUtils() {}

    public static <
                    FP extends GenericFeature<P, FP>,
                    P,
                    TP extends GenericFeatureCollection<FP, P, TP>,
                    FR extends GenericFeature<R, FR>,
                    R,
                    TR extends GenericFeatureCollection<FR, R, TR>>
            GenericFeatureCollection<FR, R, TR> map(
                    final GenericFeatureCollection<FP, P, TP> features,
                    final Function<FP, FR> mapper) {
        return new GenericFeatureCollection<FR, R, TR>()
                .setFeatures(features.getFeatures().stream().map(mapper).toList());
    }
}
