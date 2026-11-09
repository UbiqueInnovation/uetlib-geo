package ch.uetlib.geo.spatial;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class DuplicateAwareTypedKdTreeTest {

    @Test
    void testInsertSingleItem() {
        // Arrange
        final var tree = new DuplicateAwareTypedKdTree<String>();
        final var coordinate = new Coordinate(10.0, 15.0);
        final var item = "Item1";

        // Act
        tree.insert(coordinate, item);

        // Assert
        assertFalse(tree.isEmpty());
        assertEquals(1, tree.size());
        final var result =
                tree.query(new org.locationtech.jts.geom.Envelope(9.0, 11.0, 14.0, 16.0));
        assertTrue(result.contains(item));
    }

    @Test
    void testInsertMultipleItemsWithDifferentCoordinates() {
        // Arrange
        final var tree = new DuplicateAwareTypedKdTree<String>();
        final var coordinate1 = new Coordinate(5.0, 10.0);
        final var coordinate2 = new Coordinate(15.0, 20.0);
        final var item1 = "Item1";
        final var item2 = "Item2";

        // Act
        tree.insert(coordinate1, item1);
        tree.insert(coordinate2, item2);

        // Assert
        assertFalse(tree.isEmpty());
        assertEquals(2, tree.size());
        final var result1 = tree.query(new org.locationtech.jts.geom.Envelope(4.0, 6.0, 9.0, 11.0));
        final var result2 =
                tree.query(new org.locationtech.jts.geom.Envelope(14.0, 16.0, 19.0, 21.0));
        assertTrue(result1.contains(item1));
        assertTrue(result2.contains(item2));
    }

    @Test
    void testInsertDuplicateCoordinates() {
        // Arrange
        final var tree = new DuplicateAwareTypedKdTree<String>();
        final var coordinate = new Coordinate(10.0, 10.0);
        final var item1 = "Item1";
        final var item2 = "Item2";

        // Act
        tree.insert(coordinate, item1);
        tree.insert(coordinate, item2);

        // Assert
        assertFalse(tree.isEmpty());
        assertEquals(2, tree.size());
        final var result = tree.query(new org.locationtech.jts.geom.Envelope(9.0, 11.0, 9.0, 11.0));
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
    }

    @Test
    void testInsertItemsOutOfQueryBounds() {
        // Arrange
        final var tree = new DuplicateAwareTypedKdTree<String>();
        final var coordinate = new Coordinate(50.0, 50.0);
        final var item = "Item1";

        // Act
        tree.insert(coordinate, item);

        // Assert
        final var result =
                tree.query(new org.locationtech.jts.geom.Envelope(10.0, 20.0, 10.0, 20.0));
        assertTrue(result.isEmpty());
    }
}
