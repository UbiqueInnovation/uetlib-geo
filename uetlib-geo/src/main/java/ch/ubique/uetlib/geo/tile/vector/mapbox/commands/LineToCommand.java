package ch.ubique.uetlib.geo.tile.vector.mapbox.commands;

import java.util.List;

public record LineToCommand(List<Delta> deltas) implements Command {

    @Override
    public CommandKey key() {
        return CommandKey.LINE_TO;
    }
}
