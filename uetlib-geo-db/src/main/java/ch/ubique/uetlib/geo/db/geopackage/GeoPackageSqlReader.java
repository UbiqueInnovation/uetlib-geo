package ch.ubique.uetlib.geo.db.geopackage;

import ch.ubique.uetlib.geo.db.model.ColumnInfo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class GeoPackageSqlReader {

    private final JdbcTemplate jdbcTemplate;

    public GeoPackageSqlReader(final Path geoPackagePath) {
        final var url = "jdbc:sqlite:" + geoPackagePath.toAbsolutePath();
        final var dataSource = new DriverManagerDataSource(url);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }

    public Collection<String> getAttributeTables() {
        return jdbcTemplate
                .queryForList(
                        "SELECT table_name FROM gpkg_contents WHERE data_type = 'attributes'",
                        String.class)
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    public Collection<ColumnInfo> columnsInfos(final String tableName) {
        return jdbcTemplate.query(
                "SELECT * FROM pragma_table_info(?)",
                (rs, _) ->
                        new ColumnInfo(
                                rs.getString("name"),
                                rs.getString("type"),
                                rs.getBoolean("notnull")),
                tableName);
    }
}
