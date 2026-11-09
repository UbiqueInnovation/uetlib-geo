package ch.uetlib.geo.db.mbtile.utils;

import ch.uetlib.db.sqlite.SQLiteDB;
import ch.uetlib.db.sqlite.SQLiteHelper;
import ch.uetlib.geo.db.mbtile.data.impl.JdbcMbtileDataServiceImpl;
import ch.uetlib.geo.mbtile.metadata.constants.MbtileFormat;
import ch.uetlib.geo.mbtile.metadata.constants.MetadataKey;
import ch.uetlib.geo.tile.common.constants.SwissConstants;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

public final class MbtileHelper {

    private MbtileHelper() {}

    private static final String MBTILE_DB_SCHEMA_PATH = "ch/uetlib/geo/mbtile/data/schema.sql";
    private static final String MBTILES_EXTENSION = ".mbtiles";

    public static SQLiteDB getInitializedChPbfDb(
            final String name, final int version, final VectorTileConfig config)
            throws SQLException, IOException, ClassNotFoundException {
        return getInitializedChDb(
                name,
                version,
                MbtileFormat.PBF,
                Collections.min(config.zoomLevels()),
                Collections.max(config.zoomLevels()));
    }

    public static SQLiteDB getInitializedChDb(
            final String name,
            final int version,
            final MbtileFormat mbtileFormat,
            final int minZoom,
            final int maxZoom)
            throws SQLException, IOException, ClassNotFoundException {
        final var sqliteDb = getUninitializedDb(name, version);
        final var dataSource = sqliteDb.dataSource();
        final var dataService = new JdbcMbtileDataServiceImpl(dataSource);

        dataService.insertReplaceMetadataName(name);
        dataService.insertReplaceMetadataFormat(mbtileFormat);

        final var centerOfCh = SwissConstants.getCenterOfCh();
        final var zoomLevelOfDefaultView = 10;
        dataService.insertReplaceMetadata(
                MetadataKey.CENTER,
                String.format(
                        "%f,%f,%d",
                        centerOfCh.latitude(), centerOfCh.longitude(), zoomLevelOfDefaultView));

        dataService.insertReplaceMetadata(MetadataKey.VERSION, version);

        dataService.insertReplaceMetadata(MetadataKey.MINZOOM, minZoom);
        dataService.insertReplaceMetadata(MetadataKey.MAXZOOM, maxZoom);

        try (final var connection = dataSource.getConnection()) {
            connection.commit();
        }
        return sqliteDb;
    }

    public static SQLiteDB getUninitializedDb(final String name, final int version)
            throws SQLException, IOException, ClassNotFoundException {
        return SQLiteHelper.createSQLiteDbInTempDir(
                name + MBTILES_EXTENSION, MBTILE_DB_SCHEMA_PATH, version);
    }
}
