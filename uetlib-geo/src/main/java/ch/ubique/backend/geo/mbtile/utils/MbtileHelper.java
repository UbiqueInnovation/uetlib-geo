package ch.ubique.backend.geo.mbtile.utils;

import ch.ubique.backend.db.sqlite.SQLiteHelper;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.mbtile.data.impl.JdbcMbtileDataServiceImpl;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

public class MbtileHelper {
    private static final String MBTILE_DB_SCHEMA_PATH =
            "ch/ubique/backend/geo/mbtile/data/schema.sql";
    private static final String MBTILES_EXTENSION = ".mbtiles";

    public static DataSource getInitializedChPbfDb(
            final String name, final int version, final VectorTileConfig config)
            throws SQLException, IOException, ClassNotFoundException {
        return getInitializedChDb(
                name,
                version,
                MbtileFormat.PBF,
                Collections.min(config.getZoomLevels()),
                Collections.max(config.getZoomLevels()));
    }

    public static DataSource getInitializedChDb(
            final String name,
            final int version,
            final MbtileFormat mbtileFormat,
            int minZoom,
            int maxZoom)
            throws SQLException, IOException, ClassNotFoundException {
        final DataSource dataSource = getUninitializedDb(name, version);
        final JdbcMbtileDataServiceImpl dataService = new JdbcMbtileDataServiceImpl(dataSource);

        dataService.insertReplaceMetadataName(name);
        dataService.insertReplaceMetadataFormat(mbtileFormat);

        final WGS84Coordinate centerOfCh = SwissConstants.getCenterOfCh();
        final int zoomLevelOfDefaultView = 10;
        dataService.insertReplaceMetadata(
                MetadataKey.CENTER,
                String.format(
                        "(%f,%f,%d)",
                        centerOfCh.getLongitude(),
                        centerOfCh.getLatitude(),
                        zoomLevelOfDefaultView));

        dataService.insertReplaceMetadata(MetadataKey.VERSION, version);

        dataService.insertReplaceMetadata(MetadataKey.MINZOOM, minZoom);
        dataService.insertReplaceMetadata(MetadataKey.MAXZOOM, maxZoom);

        dataSource.getConnection().commit();
        return dataSource;
    }

    public static DataSource getUninitializedDb(String name, int version)
            throws SQLException, IOException, ClassNotFoundException {
        return SQLiteHelper.createSQLiteDbInTempDir(
                name + MBTILES_EXTENSION, MBTILE_DB_SCHEMA_PATH, version);
    }
}
