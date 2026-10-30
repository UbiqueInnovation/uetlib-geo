package ch.ubique.uetlib.geo.tile.vector.mapbox;

import static ch.ubique.uetlib.geo.tile.vector.mapbox.decoding.CommandsDecoder.COMMANDS_DECODER_2D;
import static ch.ubique.uetlib.geo.tile.vector.mapbox.encoding.CommandsEncoder.COMMANDS_ENCODER_2D;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.uetlib.geo.tile.vector.mapbox.commands.ClosePathCommand;
import ch.ubique.uetlib.geo.tile.vector.mapbox.commands.Delta;
import ch.ubique.uetlib.geo.tile.vector.mapbox.commands.LineToCommand;
import ch.ubique.uetlib.geo.tile.vector.mapbox.commands.MoveToCommand;
import ch.ubique.uetlib.geo.tile.vector.mapbox.decoding.CommandsDecoder;
import ch.ubique.uetlib.geo.tile.vector.mapbox.encoding.CommandsEncoder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        assertEquals(commands, COMMANDS_DECODER_2D.decode(COMMANDS_ENCODER_2D.encode(commands)));
    }

    @Test
    void test3d() {
        final var commands =
                List.of(
                        new MoveToCommand(List.of(new Delta(0, 1, 10), new Delta(2, 3, 20))),
                        new LineToCommand(List.of(new Delta(4, 5, 30))),
                        new ClosePathCommand());
        assertEquals(
                commands,
                CommandsDecoder.COMMANDS_DECODER_3D.decode(
                        CommandsEncoder.COMMANDS_ENCODER_3D.encode(commands)));
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0", "1, 2", "2, 4", "3, 6", "4, 8", "5, 10", "6, 12", "7, 14", "-1, 1", "-2, 3",
        "-3, 5", "-4, 7"
    })
    void testEncodeZigZag(final int input, final int expected) {
        final var encoded = CommandsEncoder.encodeZigZag(input);
        final var encodedLong = CommandsEncoder.encodeZigZag((long) input);
        assertEquals(encoded, (int) encodedLong);
        assertEquals(expected, encoded);
        assertEquals(input, CommandsDecoder.decodeZigZag(encoded));
        assertEquals(input, CommandsDecoder.decodeZigZag(encodedLong));
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "1, 2",
        "-1, 1",
        "2147483647, 4294967294",
        "-2147483648, 4294967295",
        "2147483648, 4294967296",
        "-2147483649, 4294967297",
        "4611686018427387903, 9223372036854775806",
        "-4611686018427387904, 9223372036854775807",
        "1000000000000000000, 2000000000000000000",
        "-1000000000000000000, 1999999999999999999"
    })
    void testEncodeZigZagLong(final long input, final long expected) {
        final var encoded = CommandsEncoder.encodeZigZag(input);
        assertEquals(expected, encoded);
        assertEquals(input, CommandsDecoder.decodeZigZag(encoded));
    }

    @Test
    void testEncodeCommandInstruction() {
        assertEquals(
                9,
                CommandsEncoder.encodeCommandInstruction(
                        new MoveToCommand(
                                List.of(new Delta(0, 0))))); // count 1, id 1: 1 << 3 | 1 = 9
        assertEquals(
                18,
                CommandsEncoder.encodeCommandInstruction(
                        new LineToCommand(
                                List.of(
                                        new Delta(0, 0),
                                        new Delta(1, 1))))); // count 2, id 2: 2 << 3 | 2 = 18
        assertEquals(
                15,
                CommandsEncoder.encodeCommandInstruction(
                        new ClosePathCommand())); // count 1 (default), id 7: 1 << 3 | 7 = 15
    }
}
