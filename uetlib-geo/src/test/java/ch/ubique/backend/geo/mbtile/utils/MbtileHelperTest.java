package ch.ubique.backend.geo.mbtile.utils;

import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

class MbtileHelperTest {

    @Test
    @Disabled("for manual testing during development")
    void testInitializedChPbfDb() throws SQLException, IOException, ClassNotFoundException {
        final DataSource dataSource =
                MbtileHelper.getInitializedChDb("test", 2, MbtileFormat.PBF, 18, 20);
    }
}
