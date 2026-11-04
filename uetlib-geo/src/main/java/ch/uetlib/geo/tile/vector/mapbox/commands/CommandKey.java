package ch.uetlib.geo.tile.vector.mapbox.commands;

import java.util.Arrays;

public enum CommandKey {
    MOVE_TO(1),
    LINE_TO(2),
    CLOSE_PATH(7);

    private final int value;

    CommandKey(final int value) {
        this.value = value;
    }

    public static CommandKey parse(final int value) {
        return Arrays.stream(CommandKey.values())
                .filter(c -> c.value == value)
                .findFirst()
                .orElseThrow();
    }

    public int value() {
        return value;
    }
}
