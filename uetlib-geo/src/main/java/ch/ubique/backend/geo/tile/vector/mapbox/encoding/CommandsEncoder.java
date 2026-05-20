package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CommandsEncoder {

    private final boolean encodeZ;

    public static final CommandsEncoder COMMANDS_ENCODER_2D = new CommandsEncoder(false);
    public static final CommandsEncoder COMMANDS_ENCODER_3D = new CommandsEncoder(true);

    private CommandsEncoder(final boolean encodeZ) {
        this.encodeZ = encodeZ;
    }

    public List<Integer> encode(final List<? extends Command> commands) {
        return commands.stream().map(this::encode).flatMap(Collection::stream).toList();
    }

    private List<Integer> encode(final Command command) {
        final var integers = new ArrayList<Integer>();
        integers.add(encodeCommandInstruction(command));
        integers.addAll(encodeDeltas(command.deltas()));
        return integers;
    }

    public List<Integer> encodeDeltas(final List<Delta> deltas) {
        final var numComponents = encodeZ ? 3 : 2;
        final var integers = new ArrayList<Integer>(deltas.size() * numComponents);
        for (final var delta : deltas) {
            integers.add(encodeZigZag(delta.dx()));
            integers.add(encodeZigZag(delta.dy()));
            if (encodeZ) {
                final var dz = Objects.requireNonNull(delta.dz(), "dz must not be null");
                integers.add(encodeZigZag(dz));
            }
        }
        return integers;
    }

    public static int encodeCommandInstruction(final Command command) {
        final var commandId = command.key().value();
        return command.count() << 3 | commandId;
    }

    public static int encodeZigZag(final int value) {
        return (value << 1) ^ (value >> 31);
    }

    public static long encodeZigZag(final long value) {
        return (value << 1) ^ (value >> 63);
    }
}
