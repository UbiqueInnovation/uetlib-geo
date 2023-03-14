-- db schema according to mbtiles v1.3 specifications (https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md)

CREATE TABLE metadata
(
    name  text NOT NULL,
    value text NOT NULL
);

CREATE TABLE tiles
(
    zoom_level  integer NOT NULL,
    tile_column integer NOT NULL,
    tile_row    integer NOT NULL,
    tile_data   blob    NOT NULL
);
CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row);