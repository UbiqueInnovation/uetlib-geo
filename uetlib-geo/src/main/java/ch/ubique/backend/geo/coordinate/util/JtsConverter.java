package ch.ubique.backend.geo.coordinate.util;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.*;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeatureCollection;

import jakarta.validation.constraints.NotNull;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class JtsConverter<C extends ch.ubique.backend.geo.coordinate.models.Coordinate> {

    public static final JtsConverter<WGS84Coordinate> WGS84 =
            new JtsConverter<>(CoordinateSystems.WGS84);

    public static final JtsConverter<LV95Coordinate> LV95 =
            new JtsConverter<>(CoordinateSystems.LV95);
    public static final JtsConverter<LV03Coordinate> LV03 =
            new JtsConverter<>(CoordinateSystems.LV03);
    public static final JtsConverter<WebMercatorCoordinate> WEB_MERCATOR =
            new JtsConverter<>(CoordinateSystems.WEB_MERCATOR);
    public static final JtsConverter<UTM32Coordinate> UTM32 =
            new JtsConverter<>(CoordinateSystems.UTM32);

    private static final Collection<JtsConverter<?>> ALL_CONVERTERS =
            Set.of(LV95, LV03, WGS84, WEB_MERCATOR, UTM32);

    private final CoordinateSystem<C> coordinateSystem;

    public JtsConverter(final CoordinateSystem<C> coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    @SuppressWarnings("java:S1452") // No way of knowing the generic type here
    public static JtsConverter<?> forCrs(final CoordinateReferenceSystem crs) {
        return ALL_CONVERTERS.stream()
                .filter(jtsConverter -> jtsConverter.coordinateSystem.crs().equals(crs))
                .findFirst()
                .orElseThrow();
    }

    public static <C extends ch.ubique.backend.geo.coordinate.models.Coordinate>
            JtsConverter<C> forClass(final Class<C> cls) {
        //noinspection unchecked
        return (JtsConverter<C>)
                ALL_CONVERTERS.stream()
                        .filter(c -> c.coordinateSystem.coordinateClass().equals(cls))
                        .findFirst()
                        .orElseThrow();
    }

    public static <C extends ch.ubique.backend.geo.coordinate.models.Coordinate>
            JtsConverter<C> forInstance(final C instance) {
        //noinspection unchecked
        return forClass((Class<C>) instance.getClass());
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#srid()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    public int srid() {
        return coordinateSystem.srid();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#geometryFactory()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public GeometryFactory geometryFactory() {
        return coordinateSystem.geometryFactory();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#crs()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public CoordinateReferenceSystem crs() {
        return coordinateSystem.crs();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#bounds()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public ReferencedEnvelope coordinateSystemBounds() {
        return coordinateSystem.bounds();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#minCoordinate()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public C minCoordinate() {
        return coordinateSystem.minCoordinate();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#minJtsCoordinate()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public Coordinate minCoordinateJts() {
        return coordinateSystem.minJtsCoordinate();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#maxCoordinate()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public C maxCoordinate() {
        return coordinateSystem.maxCoordinate();
    }

    /**
     * @deprecated Please use {@link CoordinateSystem#maxJtsCoordinate()} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0")
    @NotNull
    public Coordinate maxCoordinateJts() {
        return coordinateSystem.maxJtsCoordinate();
    }

    @NotNull
    public Coordinate toJts(final C coordinate) {
        return coordinateSystem.toJts(coordinate);
    }

    @NotNull
    public C toBackendLib(final Coordinate coordinate) {
        return coordinateSystem.fromJts(coordinate);
    }

    @NotNull
    public C toBackendLib(final Point point) {
        return toBackendLib(point.getCoordinate());
    }

    @NotNull
    public Point toJtsPoint(final C coordinate) {
        return coordinateSystem.geometryFactory().createPoint(toJts(coordinate));
    }

    @NotNull
    public LineString toJtsLineString(final Collection<C> coordinates) {
        return coordinateSystem.geometryFactory().createLineString(toJts(coordinates));
    }

    @NotNull
    public Coordinate[] toJts(final Collection<C> coordinates) {
        return coordinates.stream().map(this::toJts).toArray(Coordinate[]::new);
    }

    @NotNull
    public Polygon toJtsPolygon(final Collection<C> exteriorRing) {
        return toJtsPolygon(exteriorRing, Set.of());
    }

    @NotNull
    public Polygon toJtsPolygon(
            final Collection<C> exteriorRing, final Collection<List<C>> interiorRings) {
        return coordinateSystem
                .geometryFactory()
                .createPolygon(toJtsLinearRing(exteriorRing), toJtsLinearRings(interiorRings));
    }

    @NotNull
    public LinearRing toJtsLinearRing(final Collection<C> coordinates) {
        return coordinateSystem.geometryFactory().createLinearRing(toJts(coordinates));
    }

    @NotNull
    public LinearRing[] toJtsLinearRings(final Collection<List<C>> coordinates) {
        return coordinates.stream().map(this::toJtsLinearRing).toArray(LinearRing[]::new);
    }

    public MathTransform findMathTransform(final int sourceSRID, final boolean withHeight) {
        return findMathTransform(getCrs(sourceSRID), withHeight);
    }

    public MathTransform findMathTransform(
            final CoordinateReferenceSystem sourceCRS, final boolean withHeight) {
        try {
            final var baseTransform = CRS.findMathTransform(sourceCRS, coordinateSystem.crs());

            if (withHeight && 2 >= baseTransform.getTargetDimensions()) {
                // This ensures, that the height of the coordinate is passed through
                // if the base transform only transforms the first two coordinates
                return PassThroughTransform.create(0, baseTransform, 1);
            }

            return baseTransform;
        } catch (final FactoryException e) {
            // rethrow as runtime exception. no way for caller to handle this
            throw new WrappedException(e);
        }
    }

    /** Transforms the given feature into the current spatial reference system. */
    public <F extends GenericFeature<P, F>, P> F transform(final F feature) {
        final var transformedGeometry = transform(feature.getGeometry());
        return feature.setGeometry(transformedGeometry);
    }

    /** Transforms the given featureCollection into the current spatial reference system. */
    public <T extends GenericFeatureCollection<F, P, T>, F extends GenericFeature<P, F>, P>
            T transform(final T featureCollection) {
        final var transformedFeatures =
                featureCollection.getFeatures().stream().map(this::transform).toList();
        return featureCollection.setFeatures(transformedFeatures);
    }

    /** Transforms the given geometry into the current spatial reference system. */
    public Geometry transform(final Geometry geometry) {
        if (geometry.getSRID() == coordinateSystem.srid()) {
            return geometry; // No transform need, already in correct spatial reference system
        }

        try {
            final var withHeight = !Double.isNaN(geometry.getCoordinate().getZ());

            final var bounds =
                    JTS.toGeometry(coordinateSystem.bounds().transform(getCrs(geometry), false));

            final var geometryWithinBounds =
                    bounds.covers(geometry) ? geometry : geometry.intersection(bounds);

            final var transformedGeometry =
                    JTS.transform(
                            geometryWithinBounds,
                            findMathTransform(geometry.getSRID(), withHeight));

            // JTS.transform doesnt update srid
            JtsUtil.updateSrid(transformedGeometry, coordinateSystem.srid());

            return transformedGeometry;
        } catch (final TransformException | FactoryException e) {
            // rethrow as runtime exception. no way for caller to handle this
            throw new WrappedException(e);
        }
    }

    public C transform(final ch.ubique.backend.geo.coordinate.models.Coordinate coordinate) {
        return toBackendLib(transform(coordinate.toJts(), coordinate.srid()));
    }

    /** Transforms the given coordinate into the current spatial reference system. */
    public Coordinate transform(final Coordinate coordinate, final int sourceSRID) {
        return transform(coordinate, getCrs(sourceSRID));
    }

    /** Transforms the given coordinate into the current spatial reference system. */
    public Coordinate transform(
            final Coordinate coordinate, final CoordinateReferenceSystem sourceCRS) {
        if (sourceCRS.equals(coordinateSystem.crs())) {
            return coordinate; // No transform need, already in correct spatial reference system
        }
        try {
            final var withHeight = !Double.isNaN(coordinate.getZ());
            return JTS.transform(coordinate, null, findMathTransform(sourceCRS, withHeight));
        } catch (final TransformException e) {
            // rethrow as runtime exception. no way for caller to handle this
            throw new WrappedException(e);
        }
    }

    public ReferencedEnvelope transform(final ReferencedEnvelope envelope) {
        return transform(envelope, envelope.getCoordinateReferenceSystem());
    }

    public ReferencedEnvelope transform(final Envelope envelope, final int sourceSRID) {
        return transform(envelope, getCrs(sourceSRID));
    }

    public ReferencedEnvelope transform(
            final Envelope envelope, final CoordinateReferenceSystem sourceCRS) {
        return new ReferencedEnvelope(
                new Envelope(
                        transform(JtsUtil.minCoordinate(envelope), sourceCRS),
                        transform(JtsUtil.maxCoordinate(envelope), sourceCRS)),
                coordinateSystem.crs());
    }

    public <T extends ch.ubique.backend.geo.coordinate.models.Coordinate> BoundingBox<C> transform(
            final BoundingBox<T> boundingBox) {
        return new BoundingBox<>(transform(boundingBox.min()), transform(boundingBox.max()));
    }
}
