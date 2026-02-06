package ch.ubique.backend.geo.tile.vector.mapbox.optimization;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileFeature;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileLayer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VectorTileOptimizer {

    private VectorTileOptimizer() {}

    public static VectorTile optimize(final VectorTile vectorTile) {
        return new VectorTile(
                vectorTile.layers().stream().map(VectorTileOptimizer::optimize).toList());
    }

    public static VectorTileLayer optimize(final VectorTileLayer layer) {
        return mergeFeatures(layer);
    }

    private static VectorTileLayer mergeFeatures(final VectorTileLayer layer) {

        record Key(long id, Map<String, Object> tags) {
            public static Key of(final VectorTileFeature feature) {
                return new Key(feature.id(), feature.tags());
            }
        }

        final var groups = new HashMap<Key, Collection<VectorTileFeature>>();
        final var positions = new HashMap<Key, Collection<Integer>>();

        var index = 0;
        for (final var feature : layer.features()) {
            final var key = Key.of(feature);
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(feature);
            positions.computeIfAbsent(key, ignored -> new ArrayList<>()).add(index);
            index++;
        }

        final var combinedFeatures =
                positions.entrySet().stream()
                        .sorted(Comparator.comparing(entry -> average(entry.getValue())))
                        .map(Map.Entry::getKey)
                        .map(groups::get)
                        .flatMap(VectorTileOptimizer::combineGeometries)
                        .toList();

        return layer.withFeatures(combinedFeatures);
    }

    private static Stream<VectorTileFeature> combineGeometries(
            final Collection<VectorTileFeature> vectorTileFeatures) {

        if (vectorTileFeatures.isEmpty()) {
            return Stream.empty();
        }
        if (vectorTileFeatures.size() == 1) {
            return Stream.of(vectorTileFeatures.iterator().next());
        }

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

    private static double average(final Collection<Integer> integers) {
        return integers.stream().mapToInt(i -> i).average().orElseThrow();
    }
}
