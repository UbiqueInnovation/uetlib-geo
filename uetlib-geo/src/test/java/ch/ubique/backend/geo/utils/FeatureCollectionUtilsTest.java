package ch.ubique.backend.geo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.Map;

class FeatureCollectionUtilsTest {
    @Test
    void testMap() {
        final var polygons =
                Map.ofEntries(
                        Map.entry(
                                "1",
                                CoordinateSystems.WGS84
                                        .geometryFactory()
                                        .createPolygon(
                                                new Coordinate[] {
                                                    new Coordinate(0, 0),
                                                    new Coordinate(0, 1),
                                                    new Coordinate(1, 0),
                                                    new Coordinate(0, 0)
                                                })),
                        Map.entry(
                                "2",
                                CoordinateSystems.WGS84
                                        .geometryFactory()
                                        .createPolygon(
                                                new Coordinate[] {
                                                    new Coordinate(-1, 0),
                                                    new Coordinate(1, 1),
                                                    new Coordinate(2, 3),
                                                    new Coordinate(-1, 0)
                                                })));
        final var collection =
                new FeatureCollection()
                        .setFeatures(
                                List.of(
                                        new Feature()
                                                .setId("1")
                                                .setType("Feature")
                                                .setGeometry(
                                                        CoordinateSystems.WGS84
                                                                .geometryFactory()
                                                                .createPolygon())
                                                .setProperties(Map.of("Time", "Before")),
                                        new Feature()
                                                .setId("2")
                                                .setType("Feature")
                                                .setGeometry(
                                                        CoordinateSystems.WGS84
                                                                .geometryFactory()
                                                                .createPolygon())
                                                .setProperties(Map.of("Time", "Before"))));
        final var expected =
                new FeatureCollection()
                        .setFeatures(
                                List.of(
                                        new Feature()
                                                .setId("1")
                                                .setType("Feature")
                                                .setGeometry(polygons.get("1"))
                                                .setProperties(Map.of("Time", "After")),
                                        new Feature()
                                                .setId("2")
                                                .setType("Feature")
                                                .setGeometry(polygons.get("2"))
                                                .setProperties(Map.of("Time", "After"))));
        final var actual =
                FeatureCollectionUtils.map(
                        collection,
                        f ->
                                f.setProperties(Map.of("Time", "After"))
                                        .setGeometry(polygons.get(f.getId())));
        assertEquals(expected.getFeatures(), actual.getFeatures());
    }
}
