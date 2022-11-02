package ch.ubique.backend.geo.geotiff;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;

import java.io.IOException;
import java.io.InputStream;

public class GeoTiffParser {

    private GeoTiffParser() {}

    public static GridCoverage2D parse(final InputStream inputStream) throws IOException {
        final var reader = new GeoTiffReader(inputStream);
        return reader.read(null);
    }
}
