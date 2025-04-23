package ch.ubique.backend.geo.graph.io.geopackage;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;

import org.geotools.api.data.SimpleFeatureWriter;
import org.geotools.data.DefaultTransaction;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import javax.annotation.Nullable;

public class GraphGeopackageEncoder<V, C extends Coordinate> implements AutoCloseable {

    private final Function<V, C> vertexToCoordinate;
    private final CoordinateSystem<C> coordinateSystem;
    private final GeoPackage geoPackage;

    public GraphGeopackageEncoder(
            final Function<V, C> vertexToCoordinate, final CoordinateSystem<C> coordinateSystem)
            throws IOException {
        this(vertexToCoordinate, coordinateSystem, null);
    }

    public GraphGeopackageEncoder(
            final Function<V, C> vertexToCoordinate,
            final CoordinateSystem<C> coordinateSystem,
            @Nullable final File targetFile)
            throws IOException {
        this.coordinateSystem = coordinateSystem;
        this.vertexToCoordinate = vertexToCoordinate;
        geoPackage = new GeoPackage(getOrCreateFile(targetFile));
        geoPackage.init();
    }

    public void encode(final Graph<V, ?> graph) throws IOException {
        writeVertices(graph);
    }

    public GeoPackage geoPackage() {
        return geoPackage;
    }

    public File file() {
        return geoPackage.getFile();
    }

    private void writeVertices(final Graph<V, ?> graph) throws IOException {
        final var featureTypeBuilder = new SimpleFeatureTypeBuilder().crs(coordinateSystem.crs());
        featureTypeBuilder.setName("vertices");
        featureTypeBuilder.add("geom", Point.class);
        featureTypeBuilder.add("degree", Integer.class);
        final var featureType = featureTypeBuilder.buildFeatureType();
        final var layer = new FeatureEntry();
        layer.setBounds(computeBounds(graph));
        geoPackage.create(layer, featureType);
        try (final var transaction = new DefaultTransaction()) {
            final var writer = geoPackage.writer(layer, true, null, transaction);
            for (final var vertex : graph.getVertices()) {
                writeFeature(vertex, writer);
            }
            transaction.commit();
        }
    }

    private void writeFeature(final Vertex<V, ?> vertex, final SimpleFeatureWriter writer)
            throws IOException {
        final var outFeature = writer.next();
        final var properties = vertex.getProperties();
        outFeature.setAttribute("geom", vertexToCoordinate.apply(properties).toJtsPoint());
        outFeature.setAttribute("degree", vertex.degree());
        outFeature.getUserData().put("fid", String.valueOf(vertex.getId()));
        writer.write();
    }

    private ReferencedEnvelope computeBounds(final Graph<V, ?> graph) {
        final var bounds = new ReferencedEnvelope(coordinateSystem.crs());
        for (final var vertex : graph.getVertices()) {
            bounds.expandToInclude(vertexToCoordinate.apply(vertex.getProperties()).toJts());
        }
        return bounds;
    }

    @SuppressWarnings("java:S5443")
    private static File getOrCreateFile(final File targetFile) throws IOException {
        if (targetFile != null) {
            return targetFile;
        } else {
            return Files.createTempFile("graph", ".gpkg").toFile();
        }
    }

    @Override
    public void close() {
        geoPackage.close();
    }
}
