package ch.ubique.backend.geo.tile.vector.mapbox;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.*;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CommandsEncoder {

    private CommandsEncoder() {}

    public static List<Integer> encode(final List<? extends Command> commands) {
        return commands.stream().map(CommandsEncoder::encode).flatMap(Collection::stream).toList();
    }

    public static List<Command> decode(final List<Integer> commands) {
        final List<Command> decodedCommands = new ArrayList<>();

        final var iterator = commands.iterator();
        while (iterator.hasNext()) {
            final var decoded = decodeCommandInstruction(iterator.next());
            final var commandKey = decoded.getKey();
            final var count = decoded.getValue();

            decodedCommands.add(
                    switch (commandKey) {
                        case MOVE_TO -> new MoveToCommand(parseDeltas(iterator, count));
                        case LINE_TO -> new LineToCommand(parseDeltas(iterator, count));
                        case CLOSE_PATH -> new ClosePathCommand();
                    });
        }
        return decodedCommands;
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

    private static List<Delta> parseDeltas(final Iterator<Integer> iterator, final int count) {
        final List<Delta> deltas = new ArrayList<>();
        for (var i = 0; i < count; ++i) {
            final var dx = decodeParameterInteger(iterator.next());
            final var dy = decodeParameterInteger(iterator.next());
            deltas.add(new Delta(dx, dy));
        }
        return deltas;
    }

    private static int encodeCommandInstruction(final Command command) {
        final var commandId = command.key().value();
        return command.count() << 3 | commandId;
    }

    private static Pair<CommandKey, Integer> decodeCommandInstruction(final int commandInteger) {
        final var commandId = commandInteger & 0x7;
        final var count = commandInteger >> 3;
        final var commandKey = CommandKey.parse(commandId);
        return Pair.of(commandKey, count);
    }

    private static int decodeParameterInteger(final int encoded) {
        return ((encoded >> 1) ^ (-(encoded & 1)));
    }

    private static int encodeParameterInteger(final int value) {
        return (value << 1) ^ (value >> 31);
    }
}
