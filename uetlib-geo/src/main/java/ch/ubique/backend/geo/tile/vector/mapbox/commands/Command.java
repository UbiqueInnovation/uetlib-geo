package ch.ubique.backend.geo.tile.vector.mapbox.commands;

import java.util.List;

public sealed interface Command permits MoveToCommand, LineToCommand, ClosePathCommand {

    CommandKey key();

    List<Delta> deltas();

    default int count() {
        return deltas().size();
    }
}
