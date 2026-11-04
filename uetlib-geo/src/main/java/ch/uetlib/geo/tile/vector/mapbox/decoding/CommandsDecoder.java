package ch.uetlib.geo.tile.vector.mapbox.decoding;

import ch.uetlib.geo.tile.vector.mapbox.commands.*;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommandsDecoder {

    private final boolean decodeZ;

    public static final CommandsDecoder COMMANDS_DECODER_2D = new CommandsDecoder(false);
    public static final CommandsDecoder COMMANDS_DECODER_3D = new CommandsDecoder(true);

    private CommandsDecoder(final boolean decodeZ) {
        this.decodeZ = decodeZ;
    }

    public List<Command> decode(final List<Integer> commands) {
        final List<Command> decodedCommands = new ArrayList<>();

        final var iterator = commands.iterator();
        while (iterator.hasNext()) {
            final var decoded = decodeCommandInstruction(iterator.next());
            final var commandKey = decoded.getKey();
            final int count = decoded.getValue();

            decodedCommands.add(
                    switch (commandKey) {
                        case MOVE_TO -> new MoveToCommand(parseDeltas(iterator, count));
                        case LINE_TO -> new LineToCommand(parseDeltas(iterator, count));
                        case CLOSE_PATH -> new ClosePathCommand();
                    });
        }
        return decodedCommands;
    }

    private List<Delta> parseDeltas(final Iterator<Integer> iterator, final int count) {
        final List<Delta> deltas = new ArrayList<>();
        for (var i = 0; i < count; ++i) {
            final var dx = decodeZigZag(iterator.next());
            final var dy = decodeZigZag(iterator.next());
            final var dz = decodeZ ? decodeZigZag(iterator.next()) : null;
            deltas.add(new Delta(dx, dy, dz));
        }
        return deltas;
    }

    private static Pair<CommandKey, Integer> decodeCommandInstruction(final int commandInteger) {
        final var commandId = commandInteger & 0x7;
        final var count = commandInteger >> 3;
        final var commandKey = CommandKey.parse(commandId);
        return Pair.of(commandKey, count);
    }

    public static int decodeZigZag(final int encoded) {
        return ((encoded >> 1) ^ (-(encoded & 1)));
    }

    public static long decodeZigZag(final long encoded) {
        return ((encoded >> 1) ^ (-(encoded & 1)));
    }
}
