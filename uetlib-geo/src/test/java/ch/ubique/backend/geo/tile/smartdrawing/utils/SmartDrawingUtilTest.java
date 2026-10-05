package ch.ubique.backend.geo.tile.smartdrawing.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SmartDrawingUtilTest {

    @Test
    void testBooleanArrayToInt() {
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {})).isZero();
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {false})).isZero();
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {true})).isEqualTo(1);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {true, false})).isEqualTo(1);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {false, true})).isEqualTo(2);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {true, true})).isEqualTo(3);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {false, false, true}))
                .isEqualTo(4);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {true, false, true}))
                .isEqualTo(5);
        assertThat(SmartDrawingUtil.booleanArrayToInt(new boolean[] {true, true, true, true}))
                .isEqualTo(15);
    }
}
