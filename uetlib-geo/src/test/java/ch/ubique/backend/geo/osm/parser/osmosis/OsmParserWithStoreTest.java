package ch.ubique.backend.geo.osm.parser.osmosis;

import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ubique.backend.core.io.FileProvider;

import com.google.common.base.Predicates;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.io.IOException;

class OsmParserWithStoreTest {

    private OsmParserWithStore parser;
    private FileProvider fileProvider;

    @BeforeEach
    void setUp() {
        parser = new OsmParserWithStore();
        fileProvider =
                FileProvider.of(
                        () -> getClass().getResourceAsStream("/osm/liechtenstein-latest.osm.pbf"));
    }

    @Test
    void testNeedsAll() throws IOException {
        parser.needsNode(Predicates.alwaysTrue())
                .needsWay(Predicates.alwaysTrue())
                .needsRelation(Predicates.alwaysTrue())
                .onNode(this::verifyNode)
                .onWay(this::verifyWay)
                .onRelation(this::verifyRelation)
                .parse(fileProvider);
    }

    private void verifyNode(final Node node) {
        assertNotNull(node);
        final var nodeCoordinate = OsmUtils.getCoordinate(node);
        assertNotNull(nodeCoordinate);
        final var storedCoordinate = parser.getNodeCoordinate(node.getId());
        final var representativeCoordinate = parser.getRepresentativeCoordinate(node);
        assertEquals(nodeCoordinate, storedCoordinate);
        assertEquals(nodeCoordinate, representativeCoordinate);
    }

    private void verifyWay(final Way way) {
        assertNotNull(way);
        assertNotNull(parser.getRepresentativeCoordinate(way));
        assertNotNull(parser.createLineString(way));
        assertThatCollection(way.getWayNodes())
                .allMatch(node -> parser.getNodeCoordinate(node.getNodeId()) != null);
    }

    private void verifyRelation(final Relation relation) {
        assertNotNull(relation);
        var hasAnyGeometries = false;
        for (final var member : relation.getMembers()) {
            final var lineString = parser.createLineString(member);
            if (parser.hasWayNodes(member)) {
                assertNotNull(lineString);
                hasAnyGeometries = true;
            } else {
                assertNull(lineString);
            }
        }
        if (hasAnyGeometries) {
            assertNotNull(parser.getRepresentativeCoordinate(relation));
        }
    }

    @Test
    void testNeedsNone() throws IOException {
        parser.needsWay(Predicates.alwaysFalse())
                .needsRelation(Predicates.alwaysFalse())
                .onNode(Assertions::assertNotNull)
                .onWay(
                        way -> {
                            assertFalse(parser.hasWayNodes(way));
                            assertNull(parser.createLineString(way));
                            assertNull(parser.getRepresentativeCoordinate(way));
                        })
                .parse(fileProvider);

        assertThrows(IllegalStateException.class, () -> parser.getRepresentativeCoordinate(null));
        final var dummyBounds = new Bound("");
        assertThrows(
                IllegalStateException.class, () -> parser.getRepresentativeCoordinate(dummyBounds));
    }

    @Test
    void testNeedsEvenNode() throws IOException {
        parser.needsNode(node -> node.getId() % 2 == 0)
                .onNode(
                        node -> {
                            final var storedCoordinate = parser.getNodeCoordinate(node.getId());
                            if (node.getId() % 2 == 0) {
                                assertNotNull(storedCoordinate);
                            } else {
                                assertNull(storedCoordinate);
                            }
                        })
                .parse(fileProvider);
    }

    @Test
    void testNeedsEvenWay() throws IOException {
        parser.needsWay(way -> way.getId() % 2 == 0)
                .onWay(
                        way -> {
                            final var anyCoordinate = parser.getRepresentativeCoordinate(way);
                            final var lineString = parser.createLineString(way);
                            if (way.getId() % 2 == 0) {
                                assertNotNull(anyCoordinate);
                                assertNotNull(lineString);
                            } else {
                                assertNull(anyCoordinate);
                                assertNull(lineString);
                            }
                        })
                .parse(fileProvider);
    }

    @Test
    void relationRepresentativeCoordinateIsInsideConcavePolygon() throws ParseException {
        final var polygon =
                (Polygon)
                        new WKTReader()
                                .read(
                                        "POLYGON ((7 46, 7.04 46, 7.04 46.04, 7.03 46.04, "
                                                + "7.03 46.01, 7.01 46.01, 7.01 46.04, "
                                                + "7 46.04, 7 46))");
        final var geometry = polygon.getFactory().createMultiPolygon(new Polygon[] {polygon});
        assertFalse(geometry.covers(geometry.getCentroid()));

        final var representativeCoordinate =
                OsmParserWithStore.getAreaRepresentativeCoordinate(geometry);
        assertTrue(geometry.covers(representativeCoordinate.toJtsPoint()));
    }

    @Test
    void relationRepresentativeCoordinateKeepsCentroidWhenItIsInside() throws ParseException {
        final var polygon =
                (Polygon) new WKTReader().read("POLYGON ((7 46, 8 46, 8 47, 7 47, 7 46))");
        final var geometry = polygon.getFactory().createMultiPolygon(new Polygon[] {polygon});
        final var centroid = geometry.getCentroid();

        final var representativeCoordinate =
                OsmParserWithStore.getAreaRepresentativeCoordinate(geometry);

        assertEquals(centroid.getX(), representativeCoordinate.longitude());
        assertEquals(centroid.getY(), representativeCoordinate.latitude());
    }
}
