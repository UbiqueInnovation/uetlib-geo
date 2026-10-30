package ch.ubique.uetlib.geo.osm.parser.osmosis;

import static org.assertj.core.api.MapAssert.assertThatMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OsmParserTest {

    @Test
    void test() {
        final var parser =
                OsmParser.from(getClass().getResourceAsStream("/osm/liechtenstein-latest.osm.pbf"));

        final var nodesCounter = new AtomicInteger();
        final var waysCounter = new AtomicInteger();
        final var relationsCounter = new AtomicInteger();
        final var metadataCounter = new AtomicInteger();
        final var boundCounter = new AtomicInteger();
        final var completeCounter = new AtomicInteger();
        final var closeCounter = new AtomicInteger();
        parser.onNode(
                        node -> {
                            assertNotNull(node);
                            final var coordinate = OsmUtils.getCoordinate(node);
                            assertNotNull(coordinate);
                            nodesCounter.incrementAndGet();
                        })
                .onWay(
                        way -> {
                            assertNotNull(way);
                            waysCounter.incrementAndGet();
                        })
                .onRelation(
                        relation -> {
                            assertNotNull(relation);
                            relationsCounter.incrementAndGet();
                        })
                .onMetadata(
                        map -> {
                            assertNotNull(map);
                            assertThatMap(map).isNotEmpty();
                            metadataCounter.incrementAndGet();
                        })
                .onBound(
                        bound -> {
                            assertNotNull(bound);
                            assertEquals(9.471078, bound.getLeft());
                            assertEquals(9.636217, bound.getRight());
                            assertEquals(47.047740000000005, bound.getBottom());
                            assertEquals(47.271280000000004, bound.getTop());
                            boundCounter.incrementAndGet();
                        })
                .onComplete(completeCounter::incrementAndGet)
                .onClose(closeCounter::incrementAndGet)
                .parse();

        assertEquals(329_344, nodesCounter.get());
        assertEquals(36_494, waysCounter.get());
        assertEquals(939, relationsCounter.get());
        assertEquals(1, metadataCounter.get());
        assertEquals(1, boundCounter.get());
        assertEquals(1, closeCounter.get());
        assertEquals(1, closeCounter.get());
    }
}
