package ch.ubique.backend.geo.tile.vector.mapbox;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.ClosePathCommand;
import ch.ubique.backend.geo.tile.vector.mapbox.commands.Delta;
import ch.ubique.backend.geo.tile.vector.mapbox.commands.LineToCommand;
import ch.ubique.backend.geo.tile.vector.mapbox.commands.MoveToCommand;

import org.junit.jupiter.api.Test;

import java.util.List;

class CommandsEncoderTest {

    @Test
    void test() {
        final var commands =
                List.of(
                        new MoveToCommand(List.of()),
                        new MoveToCommand(List.of(new Delta(0, 1), new Delta(2, 3))),
                        new LineToCommand(List.of()),
                        new LineToCommand(List.of(new Delta(4, 5), new Delta(5, 6))),
                        new LineToCommand(List.of(new Delta(-1, -2))),
                        new LineToCommand(List.of(new Delta(Short.MAX_VALUE, Short.MIN_VALUE))),
                        new ClosePathCommand());
        assertEquals(commands, CommandsEncoder.decode(CommandsEncoder.encode(commands)));
    }
}
