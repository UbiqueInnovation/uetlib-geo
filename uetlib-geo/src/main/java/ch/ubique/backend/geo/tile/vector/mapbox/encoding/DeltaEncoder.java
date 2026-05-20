package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.coordinate.models.IntCoordinate;
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

    public List<Delta> visitCoordinates(final List<IntCoordinate> coordinates) {
        final var deltas = new ArrayList<Delta>();
        for (final var coordinate : coordinates) {
            deltas.add(visitCoordinate(coordinate));
        }
        return deltas;
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
        return visitCoordinate(new IntCoordinate(coordinate));
    }

    private Delta visitCoordinate(final IntCoordinate coordinate) {
        final var px = coordinate.x();
        final var py = coordinate.y();
        final var pz = encodeZ ? coordinate.requireZ() : 0;
        final var delta = new Delta(px - cx, py - cy, encodeZ ? (pz - cz) : null);
        cx = px;
        cy = py;
        cz = pz;
        return delta;
    }
}
