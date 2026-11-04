package ch.uetlib.geo.db.mbtile.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ch.uetlib.geo.mbtile.metadata.constants.MbtileFormat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MbtileHelperTest {

    @Test
    @Disabled("for manual testing during development")
    void testInitializedChPbfDb() {
        assertDoesNotThrow(
                () -> MbtileHelper.getInitializedChDb("test", 2, MbtileFormat.PBF, 18, 20));
    }
}
