package ch.uetlib.geo.tile.vector.mapbox.commands;

import java.util.List;

public record ClosePathCommand() implements Command {

    @Override
    public CommandKey key() {
        return CommandKey.CLOSE_PATH;
    }

    @Override
    public List<Delta> deltas() {
        return List.of();
    }

    @Override
    public int count() {
        return 1;
    }
}
