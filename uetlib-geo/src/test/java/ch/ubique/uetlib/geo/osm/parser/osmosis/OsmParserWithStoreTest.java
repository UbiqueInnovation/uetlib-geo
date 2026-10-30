package ch.ubique.uetlib.geo.osm.parser.osmosis;

import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.ubique.uetlib.core.io.FileProvider;
import ch.ubique.uetlib.geo.coordinate.util.JtsConverter;
import ch.ubique.uetlib.geo.coordinate.util.JtsUtil;
import com.google.common.base.Predicates;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

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
        final var lineString = parser.createLineString(way);
        assertNotNull(lineString);
        assertEquals(
                JtsConverter.WGS84.toBackendLib(JtsUtil.representativeCoordinate(lineString)),
                parser.getRepresentativeCoordinate(way));
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
        final var geometry = parser.getMultiPolygon(relation);
        if (!geometry.isEmpty()) {
            final var expected =
                    JtsConverter.WGS84.toBackendLib(JtsUtil.representativeCoordinate(geometry));
            final var actual = parser.getRepresentativeCoordinate(relation);
            assertNotNull(actual);
            assertEquals(expected.longitude(), actual.longitude(), 1e-12);
            assertEquals(expected.latitude(), actual.latitude(), 1e-12);
        } else if (hasAnyGeometries) {
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
}
