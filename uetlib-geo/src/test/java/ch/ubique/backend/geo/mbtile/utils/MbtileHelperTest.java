package ch.ubique.backend.geo.mbtile.utils;

import ch.ubique.backend.db.sqlite.SQLiteDB;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

class MbtileHelperTest {

    @Test
    @Disabled("for manual testing during development")
    void testInitializedChPbfDb() throws SQLException, IOException, ClassNotFoundException {
        final SQLiteDB sqliteDb =
                MbtileHelper.getInitializedChDb("test", 2, MbtileFormat.PBF, 18, 20);
    }
}
