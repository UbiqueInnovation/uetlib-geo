package ch.ubique.backend.geodb.mbtile.data.impl;

import ch.ubique.backend.geo.mbtile.TmsTile;
import ch.ubique.backend.geo.mbtile.data.MbtileDataService;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.backend.geo.mbtile.metadata.json.MetadataJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

public class JdbcMbtileDataServiceImpl implements MbtileDataService {

    private final NamedParameterJdbcTemplate jt;
    private final SimpleJdbcInsert metadataInsert;
    private final SimpleJdbcInsert tileInsert;

    public JdbcMbtileDataServiceImpl(final DataSource dataSource) {
        this.jt = new NamedParameterJdbcTemplate(dataSource);
        this.metadataInsert = new SimpleJdbcInsert(dataSource).withTableName("metadata");
        this.tileInsert = new SimpleJdbcInsert(dataSource).withTableName("tiles");
    }

    private static MapSqlParameterSource createMetadataParams(
            final MetadataKey key, final String value) {
        return new MapSqlParameterSource()
                .addValue("name", key.getValue())
                .addValue("value", value);
    }

    private static MapSqlParameterSource[] createTileParams(final List<TmsTile> tiles) {
        return tiles.stream()
                .map(JdbcMbtileDataServiceImpl::createTileParams)
                .toArray(MapSqlParameterSource[]::new);
    }

    private static MapSqlParameterSource createTileParams(final TmsTile tile) {
        return new MapSqlParameterSource()
                .addValue("zoom_level", tile.zoomLevel())
                .addValue("tile_column", tile.column())
                .addValue("tile_row", tile.row())
                .addValue("tile_data", new SqlLobValue(tile.data()), Types.BLOB);
    }

    @Override
    @Transactional
    public void insertReplaceMetadata(final MetadataKey key, final String value) {
        if (Boolean.TRUE.equals(metadataExists(key))) {
            deleteMetadata(key);
        }
        metadataInsert.execute(createMetadataParams(key, value));
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertReplaceMetadataName(final String name) {
        insertReplaceMetadata(MetadataKey.NAME, name);
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertReplaceMetadataFormat(final MbtileFormat format) {
        insertReplaceMetadata(MetadataKey.FORMAT, format.getValue());
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertReplaceMetadataJson(final MetadataJson json) throws JsonProcessingException {
        insertReplaceMetadata(MetadataKey.JSON, new ObjectMapper().writeValueAsString(json));
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertReplaceMetadata(final MetadataKey key, final int value) {
        insertReplaceMetadata(key, String.valueOf(value));
    }

    private void deleteMetadata(final MetadataKey key) {
        jt.update(
                "delete from metadata where name = :name",
                new MapSqlParameterSource("name", key.getValue()));
    }

    private Boolean metadataExists(final MetadataKey key) {
        return jt.queryForObject(
                "select exists(select * from metadata where name = :name)",
                new MapSqlParameterSource("name", key.getValue()),
                Boolean.class);
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertTile(final TmsTile tiles) {
        insertTiles(List.of(tiles));
    }

    @Override
    @Transactional
    @SuppressWarnings("java:S6809")
    public void insertTiles(final List<TmsTile> tiles) {
        // only store non-empty tiles
        final var nonEmptyTiles =
                tiles.stream().filter(t -> t.data() != null && t.data().length > 0).toList();
        if (!nonEmptyTiles.isEmpty()) {
            tileInsert.executeBatch(createTileParams(nonEmptyTiles));
        }
    }
}
