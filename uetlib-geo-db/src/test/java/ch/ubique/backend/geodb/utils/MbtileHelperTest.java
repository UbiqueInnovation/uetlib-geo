package ch.ubique.backend.geodb.utils;

import ch.ubique.backend.db.sqlite.SQLiteDB;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geodb.mbtile.utils.MbtileHelper;

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
