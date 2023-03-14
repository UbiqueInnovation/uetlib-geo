package ch.ubique.backend.geo.mbtile.data.impl;

import ch.ubique.backend.geo.mbtile.TmsTile;
import ch.ubique.backend.geo.mbtile.data.MbtileDataService;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.backend.geo.mbtile.metadata.json.MetadataJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

public class JdbcMbtileDataServiceImpl implements MbtileDataService {

    private static final Logger logger = LoggerFactory.getLogger(JdbcMbtileDataServiceImpl.class);

    private final NamedParameterJdbcTemplate jt;
    private final SimpleJdbcInsert metadataInsert;
    private final SimpleJdbcInsert tileInsert;

    public JdbcMbtileDataServiceImpl(DataSource dataSource) {
        this.jt = new NamedParameterJdbcTemplate(dataSource);
        this.metadataInsert = new SimpleJdbcInsert(dataSource).withTableName("metadata");
        this.tileInsert = new SimpleJdbcInsert(dataSource).withTableName("tiles");
    }

    @Override
    @Transactional(readOnly = false)
    public void insertReplaceMetadata(MetadataKey key, String value) {
        if (metadataExists(key)) {
            deleteMetadata(key);
        }
        metadataInsert.execute(createMetadataParams(key, value));
    }

    @Override
    @Transactional(readOnly = false)
    public void insertReplaceMetadataName(String name) {
        insertReplaceMetadata(MetadataKey.NAME, name);
    }

    @Override
    @Transactional(readOnly = false)
    public void insertReplaceMetadataFormat(MbtileFormat format) {
        insertReplaceMetadata(MetadataKey.FORMAT, format.getValue());
    }

    @Override
    @Transactional(readOnly = false)
    public void insertReplaceMetadataJson(MetadataJson json) throws JsonProcessingException {
        insertReplaceMetadata(MetadataKey.JSON, new ObjectMapper().writeValueAsString(json));
    }

    @Override
    @Transactional(readOnly = false)
    public void insertReplaceMetadata(MetadataKey key, int value) {
        insertReplaceMetadata(key, String.valueOf(value));
    }

    private void deleteMetadata(MetadataKey key) {
        jt.update(
                "delete from metadata where name = :name",
                new MapSqlParameterSource("name", key.getValue()));
    }

    private Boolean metadataExists(MetadataKey key) {
        return jt.queryForObject(
                "select exists(select * from metadata where name = :name)",
                new MapSqlParameterSource("name", key.getValue()),
                Boolean.class);
    }

    private String findMetadataValue(MetadataKey key) {
        try {
            return jt.queryForObject(
                    "select value from metadata where name = :name",
                    new MapSqlParameterSource("name", key.getValue()),
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static MapSqlParameterSource createMetadataParams(MetadataKey key, String value) {
        return new MapSqlParameterSource()
                .addValue("name", key.getValue())
                .addValue("value", value);
    }

    @Override
    @Transactional(readOnly = false)
    public void insertTile(TmsTile tiles) {
        insertTiles(List.of(tiles));
    }

    @Override
    @Transactional(readOnly = false)
    public void insertTiles(List<TmsTile> tiles) {
        // only store non-empty tiles
        final List<TmsTile> nonEmptyTiles =
                tiles.stream()
                        .filter(t -> t.getData() != null && t.getData().length > 0)
                        .collect(Collectors.toList());
        if (!nonEmptyTiles.isEmpty()) {
            tileInsert.executeBatch(createTileParams(nonEmptyTiles));
        }
    }

    private static MapSqlParameterSource[] createTileParams(List<TmsTile> tiles) {
        return tiles.stream().map(t -> createTileParams(t)).toArray(MapSqlParameterSource[]::new);
    }

    private static MapSqlParameterSource createTileParams(TmsTile tile) {
        return new MapSqlParameterSource()
                .addValue("zoom_level", tile.getZoomLevel())
                .addValue("tile_column", tile.getColumn())
                .addValue("tile_row", tile.getRow())
                .addValue("tile_data", new SqlLobValue(tile.getData()), Types.BLOB);
    }
}
