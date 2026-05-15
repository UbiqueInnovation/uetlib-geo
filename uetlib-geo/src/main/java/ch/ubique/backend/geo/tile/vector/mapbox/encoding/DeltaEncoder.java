package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.*;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class DeltaEncoder {

    // Current position of the cursor
    private int cx;
    private int cy;
    private int cz;

    private final boolean encodeZ;

    public DeltaEncoder(final boolean encodeZ) {
        this.encodeZ = encodeZ;
    }

    public List<Delta> visitCoordinates(final Coordinate[] coordinates) {
        final var deltas = new ArrayList<Delta>();
        for (final var coordinate : coordinates) {
            deltas.add(visitCoordinate(coordinate));
        }
        return deltas;
    }

    public List<Delta> visitCoordinates(final Iterable<Coordinate> coordinates) {
        final var deltas = new ArrayList<Delta>();
        for (final var coordinate : coordinates) {
            deltas.add(visitCoordinate(coordinate));
        }
        return deltas;
    }

    private Delta visitCoordinate(final Coordinate coordinate) {
        final var px = (int) Math.round(coordinate.x);
        final var py = (int) Math.round(coordinate.y);
        final var pz = getZ(coordinate);
        final var delta = new Delta(px - cx, py - cy, encodeZ ? (pz - cz) : null);
        cx = px;
        cy = py;
        cz = pz;
        return delta;
    }

    private int getZ(final Coordinate coordinate) {
        if (!encodeZ) {
            return 0;
        }
        final var z = coordinate.getZ();
        if (Double.isNaN(z)) {
            throw new IllegalArgumentException("Coordinate has no z component");
        }
        return (int) Math.round(z);
    }
}
