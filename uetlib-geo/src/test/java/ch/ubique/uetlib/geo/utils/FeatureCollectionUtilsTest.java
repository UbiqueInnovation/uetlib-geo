package ch.ubique.uetlib.geo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.uetlib.geo.geojson.models.jts.Feature;
import ch.ubique.uetlib.geo.geojson.models.jts.FeatureCollection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

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
        assertEquals(2, actual.getFeatures().size());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).getFirst().getId(),
                ((List<Feature>) actual.getFeatures()).getFirst().getId());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).getFirst().getType(),
                ((List<Feature>) actual.getFeatures()).getFirst().getType());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).getFirst().getGeometry(),
                ((List<Feature>) actual.getFeatures()).getFirst().getGeometry());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).getFirst().getProperties(),
                ((List<Feature>) actual.getFeatures()).getFirst().getProperties());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).get(1).getId(),
                ((List<Feature>) actual.getFeatures()).get(1).getId());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).get(1).getType(),
                ((List<Feature>) actual.getFeatures()).get(1).getType());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).get(1).getGeometry(),
                ((List<Feature>) actual.getFeatures()).get(1).getGeometry());
        assertEquals(
                ((List<Feature>) expected.getFeatures()).get(1).getProperties(),
                ((List<Feature>) actual.getFeatures()).get(1).getProperties());
    }
}
