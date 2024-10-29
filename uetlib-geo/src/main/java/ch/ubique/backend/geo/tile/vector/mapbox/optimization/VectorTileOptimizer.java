package ch.ubique.backend.geo.tile.vector.mapbox.optimization;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileFeature;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileLayer;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorTileOptimizer {

    private VectorTileOptimizer() {}

    public static Collection<VectorTileLayer> optimize(final Collection<VectorTileLayer> layers) {
        return layers.stream().map(VectorTileOptimizer::optimize).toList();
    }

    public static VectorTileLayer optimize(final VectorTileLayer layer) {
        return mergeFeatures(layer);
    }

    private static VectorTileLayer mergeFeatures(final VectorTileLayer layer) {
        final var groupedFeatures =
                layer.features().stream()
                        .collect(Collectors.groupingBy(f -> Pair.of(f.id(), f.tags())));
        final var combinedFeatures =
                groupedFeatures.values().stream()
                        .flatMap(VectorTileOptimizer::combineGeometries)
                        .toList();
        return layer.withFeatures(combinedFeatures);
    }

    private static Stream<VectorTileFeature> combineGeometries(
            final Collection<VectorTileFeature> vectorTileFeatures) {
        final var anyFeature = vectorTileFeatures.iterator().next();

        final var points = new ArrayList<Point>(0);
        final var lines = new ArrayList<Geometry>(0);
        final var polygons = new ArrayList<Polygon>(0);

        for (final var feature : vectorTileFeatures) {
            final var geometryOrCollection = feature.geometry();
            for (var i = 0; i < geometryOrCollection.getNumGeometries(); i++) {
                final var geometry = geometryOrCollection.getGeometryN(i);
                switch (geometry) {
                    case final Point point -> points.add(point);
                    case final LineString lineString -> lines.add(lineString);
                    case final Polygon polygon -> polygons.add(polygon);
                    default ->
                            throw new IllegalStateException(
                                    "Unexpected geometry type: " + geometry);
                }
            }
        }

        final var pointGeometry =
                points.size() == 1 ? points.getFirst() : JtsUtil.toJtsMultiPoint(points);
        final var lineGeometry = lines.size() == 1 ? lines.getFirst() : JtsUtil.mergeLines(lines);
        final var polygonGeometry =
                polygons.size() == 1
                        ? polygons.getFirst()
                        : JtsUtil.toJtsMultiPolygon(polygons).union();

        return Stream.of(pointGeometry, lineGeometry, polygonGeometry)
                .filter(Predicate.not(Geometry::isEmpty))
                .map(g -> new VectorTileFeature(anyFeature.id(), g, anyFeature.tags()));
    }
}
