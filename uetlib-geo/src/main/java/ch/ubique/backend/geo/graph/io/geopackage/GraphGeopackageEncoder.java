package ch.ubique.backend.geo.graph.io.geopackage;

import static java.util.Objects.requireNonNullElseGet;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.core.utils.MapCollectors;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.exception.UncheckedException;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.api.data.SimpleFeatureWriter;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DefaultTransaction;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

public class GraphGeopackageEncoder<V, C extends Coordinate> implements AutoCloseable {

    private final Function<V, C> vertexToCoordinate;
    private final CoordinateSystem<C> coordinateSystem;
    private final GeoPackage geoPackage;
    private final ObjectMapper objectMapper;

    public GraphGeopackageEncoder(
            final Function<V, C> vertexToCoordinate, final CoordinateSystem<C> coordinateSystem)
            throws IOException {
        this(vertexToCoordinate, coordinateSystem, null, null);
    }

    public GraphGeopackageEncoder(
            final Function<V, C> vertexToCoordinate,
            final CoordinateSystem<C> coordinateSystem,
            @Nullable final File targetFile,
            @Nullable final ObjectMapper objectMapper)
            throws IOException {
        this.coordinateSystem = coordinateSystem;
        this.vertexToCoordinate = vertexToCoordinate;
        this.objectMapper = requireNonNullElseGet(objectMapper, ObjectMapperUtils::objectMapper);
        geoPackage = new GeoPackage(getOrCreateFile(targetFile));
        geoPackage.init();
    }

    public void encode(final Graph<V, ?> graph) throws IOException {
        writeVertices(graph);
        writeEdges(graph);
    }

    public GeoPackage geoPackage() {
        return geoPackage;
    }

    public File file() {
        return geoPackage.getFile();
    }

    private void writeVertices(final Graph<V, ?> graph) throws IOException {
        final var featureType = createVertexFeatureType(graph);
        final var layer = new FeatureEntry();
        layer.setBounds(computeBounds(graph));
        layer.setSrid(coordinateSystem.srid());
        geoPackage.create(layer, featureType);
        try (final var transaction = new DefaultTransaction()) {
            final var writer = geoPackage.writer(layer, true, null, transaction);
            for (final var vertex : graph.getVertices()) {
                writeFeature(vertex, writer);
            }
            transaction.commit();
        }
    }

    private void writeEdges(final Graph<V, ?> graph) throws IOException {
        final var featureType = createEdgeFeatureType(graph);
        final var layer = new FeatureEntry();
        layer.setBounds(computeBounds(graph));
        layer.setSrid(coordinateSystem.srid());
        geoPackage.create(layer, featureType);
        try (final var transaction = new DefaultTransaction()) {
            final var writer = geoPackage.writer(layer, true, null, transaction);
            for (final var edge : graph.getDirectedEdges()) {
                writeFeature(edge, writer);
            }
            transaction.commit();
        }
    }

    private SimpleFeatureType createVertexFeatureType(final Graph<V, ?> graph) {
        final var featureTypeBuilder = new SimpleFeatureTypeBuilder().crs(coordinateSystem.crs());
        featureTypeBuilder.setName("vertices");
        final var attributes = new HashMap<String, Class<?>>();
        attributes.put("geom", Point.class);
        featureTypeBuilder.add("degree", Integer.class);
        for (final var vertex : graph.getVertices()) {
            toMap(vertex.getProperties())
                    .forEach(
                            (key, value) -> attributes.computeIfAbsent(key, k -> value.getClass()));
        }
        attributes.forEach(featureTypeBuilder::add);
        return featureTypeBuilder.buildFeatureType();
    }

    private void writeFeature(final Vertex<V, ?> vertex, final SimpleFeatureWriter writer)
            throws IOException {
        final var outFeature = writer.next();
        final var properties = vertex.getProperties();
        outFeature.setAttribute("geom", vertexToCoordinate.apply(properties).toJtsPoint());
        outFeature.setAttribute("degree", vertex.degree());
        toMap(properties).forEach(outFeature::setAttribute);
        outFeature.getUserData().put("fid", String.valueOf(vertex.getId()));
        writer.write();
    }

    private SimpleFeatureType createEdgeFeatureType(final Graph<V, ?> graph) {
        final var featureTypeBuilder = new SimpleFeatureTypeBuilder().crs(coordinateSystem.crs());
        featureTypeBuilder.setName("edges");

        final var additionalAttributes =
                graph.directedEdgesStream()
                        .map(Edge::getProperties)
                        .map(this::toMap)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .map(e -> Pair.of(e.getKey(), e.getValue().getClass()))
                        .collect(MapCollectors.entriesToMapDropDuplicates());

        final var attributes = new HashMap<String, Class<?>>(additionalAttributes);
        attributes.put("geom", LineString.class);
        featureTypeBuilder.add("from", Integer.class);
        featureTypeBuilder.add("to", Integer.class);

        attributes.forEach(featureTypeBuilder::add);
        return featureTypeBuilder.buildFeatureType();
    }

    private void writeFeature(final Edge<V, ?> edge, final SimpleFeatureWriter writer)
            throws IOException {
        final var outFeature = writer.next();
        final var from = edge.getFrom();
        final var to = edge.getTo();
        final var geometry =
                coordinateSystem
                        .geometryFactory()
                        .createLineString(
                                new org.locationtech.jts.geom.Coordinate[] {
                                    vertexToCoordinate.apply(from.getProperties()).toJts(),
                                    vertexToCoordinate.apply(to.getProperties()).toJts()
                                });
        toMap(edge.getProperties()).forEach(outFeature::setAttribute);
        outFeature.setAttribute("geom", geometry);
        outFeature.setAttribute("from", from.getId());
        outFeature.setAttribute("to", to.getId());
        outFeature.getUserData().put("fid", String.format("%d_%d", from.getId(), to.getId()));
        writer.write();
    }

    private ReferencedEnvelope computeBounds(final Graph<V, ?> graph) {
        final var bounds = new ReferencedEnvelope(coordinateSystem.crs());
        for (final var vertex : graph.getVertices()) {
            bounds.expandToInclude(vertexToCoordinate.apply(vertex.getProperties()).toJts());
        }
        return bounds;
    }

    private Map<String, Object> toMap(final Object object) {
        return objectMapper
                .convertValue(object, new TypeReference<Map<String, Object>>() {})
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), serializeAttributeValue(e.getValue())))
                .filter(e -> e.getValue() != null)
                .collect(MapCollectors.entriesToMap());
    }

    private Object serializeAttributeValue(final Object value) {
        return switch (value) {
            case null -> null;
            case final Boolean bool -> bool;
            case final Number number -> number;
            case final CharSequence charSequence -> charSequence.toString();
            default -> serializeWithObjectMapper(value);
        };
    }

    private String serializeWithObjectMapper(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw new UncheckedException(e);
        }
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
