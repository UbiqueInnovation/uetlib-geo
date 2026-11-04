package ch.uetlib.geo.graph.routing.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class Snapping3dPointIndexTest {

    @Test
    void snap() {
        final var tolerance = 2;
        final var index = new Snapping3dPointIndex(tolerance);

        final var c0 = new Coordinate(0, 0, 0);
        final var z10 = new Coordinate(0, 0, 10);
        assertEquals(0, c0.distance3D(index.snap(c0)));
        assertEquals(0, z10.distance3D(index.snap(z10)));

        final var z1 = new Coordinate(0, 0, 1);
        assertEquals(1, z1.distance3D(index.snap(z1)));

        final var x1 = new Coordinate(1, 0, 0);
        assertEquals(1, x1.distance3D(index.snap(x1)));

        final var z3 = new Coordinate(0, 0, 3);
        assertEquals(0, z3.distance3D(index.snap(z3)));
    }
}
