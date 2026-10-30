package ch.ubique.uetlib.geo.tile.smartdrawing.encoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

class TileCoordinateEncoderTest {

    @Test
    void testComputeTileCoordinates() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var extent = 100;
        final var encoder = new TileCoordinateEncoder(extent, envelope);

        final var gf = new GeometryFactory(new PrecisionModel(), 4326);
        final var lineString =
                gf.createLineString(
                        new Coordinate[] {
                            new Coordinate(0, 0), new Coordinate(10, 10), new Coordinate(5, 5)
                        });

        final var tileCoordinates = encoder.computeTileCoordinates(lineString);

        assertThat(tileCoordinates).hasSize(3);

        assertThat(tileCoordinates.get(0).x()).isZero();
        assertThat(tileCoordinates.get(0).y()).isEqualTo(100);

        assertThat(tileCoordinates.get(1).x()).isEqualTo(100);
        assertThat(tileCoordinates.get(1).y()).isZero();

        assertThat(tileCoordinates.get(2).x()).isEqualTo(50);
        assertThat(tileCoordinates.get(2).y()).isEqualTo(50);
    }

    @Test
    void testComputeTileCoordinatesWithElevation() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var extent = 100;
        final var encoder = new TileCoordinateEncoder(extent, envelope);

        final var gf = new GeometryFactory(new PrecisionModel(), 4326);
        final var lineString =
                gf.createLineString(
                        new Coordinate[] {
                            new Coordinate(0, 0, 1.234), new Coordinate(10, 10, Double.NaN)
                        });

        final var tileCoordinates = encoder.computeTileCoordinates(lineString);

        assertThat(tileCoordinates.get(0).z()).isEqualTo(123); // 1.234 * 100 rounded
        assertThat(tileCoordinates.get(1).z()).isNull();
    }

    @Test
    void testCrsMismatch() throws Exception {
        final var crs4326 = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs4326);
        final var encoder = new TileCoordinateEncoder(100, envelope);

        final var gf3857 = new GeometryFactory(new PrecisionModel(), 3857);
        final var lineString =
                gf3857.createLineString(
                        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});

        assertThatThrownBy(() -> encoder.computeTileCoordinates(lineString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CRS mismatch.");
    }
}
