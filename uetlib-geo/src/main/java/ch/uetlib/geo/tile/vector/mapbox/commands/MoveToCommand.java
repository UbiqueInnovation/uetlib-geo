package ch.uetlib.geo.tile.vector.mapbox.commands;

import java.util.List;

public record MoveToCommand(List<Delta> deltas) implements Command {

    @Override
    public CommandKey key() {
        return CommandKey.MOVE_TO;
    }
}
