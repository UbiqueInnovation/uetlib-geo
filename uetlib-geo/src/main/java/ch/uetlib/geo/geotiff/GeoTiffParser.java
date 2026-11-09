package ch.uetlib.geo.geotiff;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.util.factory.Hints;

import java.io.IOException;
import java.io.InputStream;

public class GeoTiffParser {

    private GeoTiffParser() {}

    public static GridCoverage2D parse(
            final InputStream inputStream, final CoordinateReferenceSystem referenceSystem)
            throws IOException {
        final var hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, referenceSystem);
        final var reader = new GeoTiffReader(inputStream, hints);
        return reader.read();
    }
}
