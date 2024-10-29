package ch.ubique.backend.geo.tile.vector.mapbox.helpers;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.Command;
import ch.ubique.backend.geo.tile.vector.mapbox.commands.Delta;

import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.stream.Stream;

public class GeometryCursor {
    private int x;
    private int y;

    public Coordinate currentCoordinate() {
        return new Coordinate(x, y);
    }

    public Coordinate move(final Delta delta) {
        x += delta.dx();
        y += delta.dy();
        return currentCoordinate();
    }

    public Stream<Coordinate> move(final List<Delta> deltas) {
        return deltas.stream().map(this::move);
    }

    public Stream<Coordinate> move(final Command command) {
        return move(command.deltas());
    }
}
