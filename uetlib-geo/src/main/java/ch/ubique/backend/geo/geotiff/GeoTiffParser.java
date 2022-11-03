package ch.ubique.backend.geo.geotiff;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.util.factory.Hints;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.io.InputStream;

public class GeoTiffParser {

    private GeoTiffParser() {}

    public static GridCoverage2D parse(final InputStream inputStream, CoordinateReferenceSystem referenceSystem) throws IOException {
        final var hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, referenceSystem);
        final var reader = new GeoTiffReader(inputStream, hints);
        return reader.read(null);
    }
}
