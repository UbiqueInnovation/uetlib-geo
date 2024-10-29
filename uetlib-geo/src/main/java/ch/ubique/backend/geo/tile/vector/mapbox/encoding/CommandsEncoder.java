package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandsEncoder {

    private CommandsEncoder() {}

    public static List<Integer> encode(final List<? extends Command> commands) {
        return commands.stream().map(CommandsEncoder::encode).flatMap(Collection::stream).toList();
    }

    private static List<Integer> encode(final Command command) {
        final var integers = new ArrayList<Integer>();
        integers.add(encodeCommandInstruction(command));
        for (final var delta : command.deltas()) {
            integers.add(encodeParameterInteger(delta.dx()));
            integers.add(encodeParameterInteger(delta.dy()));
        }
        return integers;
    }

    private static int encodeCommandInstruction(final Command command) {
        final var commandId = command.key().value();
        return command.count() << 3 | commandId;
    }

    private static int encodeParameterInteger(final int value) {
        return (value << 1) ^ (value >> 31);
    }
}
