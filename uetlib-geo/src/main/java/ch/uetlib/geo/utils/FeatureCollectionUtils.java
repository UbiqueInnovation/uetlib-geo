package ch.uetlib.geo.utils;

import ch.uetlib.geo.geojson.models.jts.GenericFeature;
import ch.uetlib.geo.geojson.models.jts.GenericFeatureCollection;

import java.util.function.Function;

public class FeatureCollectionUtils {
    private FeatureCollectionUtils() {}

    public static <
                    F1 extends GenericFeature<P1, F1>,
                    P1,
                    T1 extends GenericFeatureCollection<F1, P1, T1>,
                    F2 extends GenericFeature<P2, F2>,
                    P2,
                    T2 extends GenericFeatureCollection<F2, P2, T2>>
            GenericFeatureCollection<F2, P2, T2> map(
                    final GenericFeatureCollection<F1, P1, T1> features,
                    final Function<F1, F2> mapper) {
        return new GenericFeatureCollection<F2, P2, T2>()
                .setFeatures(features.getFeatures().stream().map(mapper).toList());
    }
}
