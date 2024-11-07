package ch.ubique.backend.geodb.mbtile.utils;

import ch.ubique.backend.db.sqlite.SQLiteDB;
import ch.ubique.backend.db.sqlite.SQLiteHelper;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geodb.mbtile.data.impl.JdbcMbtileDataServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

public final class MbtileHelper {

    private MbtileHelper() {}

    private static final String MBTILE_DB_SCHEMA_PATH =
            "ch/ubique/backend/geo/mbtile/data/schema.sql";
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
        final var dataSource = sqliteDb.getDataSource();
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

        dataSource.getConnection().commit();
        return sqliteDb;
    }

    public static SQLiteDB getUninitializedDb(final String name, final int version)
            throws SQLException, IOException, ClassNotFoundException {
        return SQLiteHelper.createSQLiteDbInTempDir(
                name + MBTILES_EXTENSION, MBTILE_DB_SCHEMA_PATH, version);
    }
}
