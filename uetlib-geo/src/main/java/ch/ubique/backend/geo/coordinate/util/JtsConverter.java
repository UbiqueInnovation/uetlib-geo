package ch.ubique.backend.geo.coordinate.util;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import static org.locationtech.jts.geom.Coordinate.NULL_ORDINATE;

import ch.ubique.backend.geo.coordinate.models.*;

import jakarta.validation.constraints.NotNull;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class JtsConverter<C extends ch.ubique.backend.geo.coordinate.models.Coordinate> {

    public static final JtsConverter<WGS84Coordinate> WGS84 =
            new JtsConverter<>(
                    4326,
                    c ->
                            new Coordinate(
                                    c.getLongitude(),
                                    c.getLatitude(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new WGS84Coordinate(c.y, c.x, nullIfNaN(c.getZ())),
                    new WGS84Coordinate(-90, -180),
                    new WGS84Coordinate(90, 180));
    /**
     * bounds equal to the min/max available coordinates with tiles on <a
     * href="https://map.geo.admin.ch/">map.geo.admin.ch</a>
     */
    public static final JtsConverter<LV95Coordinate> LV95 =
            new JtsConverter<>(
                    2056,
                    c ->
                            new Coordinate(
                                    c.getEast(),
                                    c.getNorth(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new LV95Coordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new LV95Coordinate(2_420_000.0, 1_000_000.0),
                    new LV95Coordinate(2_900_000.0, 1_350_000.0));
    /**
     * bounds equal to the min/max available coordinates with tiles on <a
     * href="https://map.geo.admin.ch/">map.geo.admin.ch</a>
     */
    public static final JtsConverter<LV03Coordinate> LV03 =
            new JtsConverter<>(
                    21781,
                    c ->
                            new Coordinate(
                                    c.getEast(),
                                    c.getNorth(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new LV03Coordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new LV03Coordinate(420_000.0, 0.0),
                    new LV03Coordinate(900_000.0, 350_000.0));

    // See https://schemas.opengis.net/wmts/1.0/profiles/wmts-simple/wmtsTileMatrixSet.json
    public static final double WEB_MERCATOR_MAX_EXTENT = 20_037_508.3427892;
    public static final JtsConverter<WebMercatorCoordinate> WEB_MERCATOR =
            new JtsConverter<>(
                    3857,
                    c ->
                            new Coordinate(
                                    c.getX(),
                                    c.getY(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new WebMercatorCoordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new WebMercatorCoordinate(-WEB_MERCATOR_MAX_EXTENT, -WEB_MERCATOR_MAX_EXTENT),
                    new WebMercatorCoordinate(WEB_MERCATOR_MAX_EXTENT, WEB_MERCATOR_MAX_EXTENT));

    private final int srid;
    private final CoordinateReferenceSystem crs;
    private final GeometryFactory geometryFactory;
    private final Function<C, Coordinate> toJtsMapper;
    private final Function<Coordinate, C> fromJtsMapper;
    private final ReferencedEnvelope coordinateSystemBounds;

    private JtsConverter(
            final int srid,
            final Function<C, Coordinate> toJtsMapper,
            final Function<Coordinate, C> fromJtsMapper,
            final C minCoordinate,
            final C maxCoordinate) {
        this.srid = srid;
        this.toJtsMapper = toJtsMapper;
        this.fromJtsMapper = fromJtsMapper;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        this.crs = getCrs(srid);
        this.coordinateSystemBounds =
                new ReferencedEnvelope(
                        new Envelope(toJts(minCoordinate), toJts(maxCoordinate)), crs);
    }

    public static JtsConverter<?> forCrs(final CoordinateReferenceSystem crs) {
        return Stream.of(LV95, LV03, WGS84, WEB_MERCATOR)
                .filter(jtsConverter -> jtsConverter.crs.equals(crs))
                .findFirst()
                .orElseThrow();
    }

    private static Double nullIfNaN(final Double value) {
        if (null != value && value.isNaN()) {
            return null;
        }
        return value;
    }

    public int srid() {
        return srid;
    }

    @NotNull
    public GeometryFactory geometryFactory() {
        return geometryFactory;
    }

    @NotNull
    public CoordinateReferenceSystem crs() {
        return crs;
    }

    @NotNull
    public ReferencedEnvelope coordinateSystemBounds() {
        return coordinateSystemBounds;
    }

    @NotNull
    public C minCoordinate() {
        return toBackendLib(minCoordinateJts());
    }

    @NotNull
    public Coordinate minCoordinateJts() {
        return new Coordinate(coordinateSystemBounds.getMinX(), coordinateSystemBounds.getMinY());
    }

    @NotNull
    public C maxCoordinate() {
        return toBackendLib(maxCoordinateJts());
    }

    @NotNull
    public Coordinate toJts(final C coordinate) {
        return toJtsMapper.apply(coordinate);
    }

    @NotNull
    public C toBackendLib(final Coordinate coordinate) {
        return fromJtsMapper.apply(coordinate);
    }

    @NotNull
    public C toBackendLib(final Point point) {
        return toBackendLib(point.getCoordinate());
    }

    @NotNull
    public Point toJtsPoint(final C coordinate) {
        return geometryFactory.createPoint(toJts(coordinate));
    }

    @NotNull
    public Coordinate maxCoordinateJts() {
        return new Coordinate(coordinateSystemBounds.getMaxX(), coordinateSystemBounds.getMaxY());
    }

    @NotNull
    public LineString toJtsLineString(final Collection<C> coordinates) {
        return geometryFactory.createLineString(toJts(coordinates));
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
        return geometryFactory.createPolygon(
                toJtsLinearRing(exteriorRing), toJtsLinearRings(interiorRings));
    }

    @NotNull
    public LinearRing toJtsLinearRing(final Collection<C> coordinates) {
        return geometryFactory.createLinearRing(toJts(coordinates));
    }

    @NotNull
    public LinearRing[] toJtsLinearRings(final Collection<List<C>> coordinates) {
        return coordinates.stream().map(this::toJtsLinearRing).toArray(LinearRing[]::new);
    }

    /** Transforms the given geometry into the current spatial reference system. */
    public Geometry transform(final Geometry geometry) {
        if (geometry.getSRID() == srid) {
            return geometry; // No transform need, already in correct spatial reference system
        }

        try {
            final var withHeight = !Double.isNaN(geometry.getCoordinate().getZ());

            final var bounds =
                    JTS.toGeometry(coordinateSystemBounds.transform(getCrs(geometry), false));

            final var geometryWithinBounds =
                    bounds.covers(geometry) ? geometry : geometry.intersection(bounds);

            final var transformedGeometry =
                    JTS.transform(
                            geometryWithinBounds,
                            findMathTransform(geometry.getSRID(), withHeight));

            // JTS.transform doesnt update srid
            JtsUtil.updateSrid(transformedGeometry, srid);

            return transformedGeometry;
        } catch (final TransformException | FactoryException e) {
            // rethrow as runtime exception. no way for caller to handle this
            throw new WrappedException(e);
        }
    }

    public MathTransform findMathTransform(final int sourceSRID, final boolean withHeight) {
        try {
            final var baseTransform = CRS.findMathTransform(getCrs(sourceSRID), crs);

            if (withHeight && 2 >= baseTransform.getTargetDimensions()) {
                // This ensures, that the height of the coordinate is passed through
                // if the base transform only transforms the first two coordinates
                return PassThroughTransform.create(0, baseTransform, 1);
            }

            return baseTransform;
        } catch (final FactoryException e) {
            throw new WrappedException(
                    e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    public C transform(final ch.ubique.backend.geo.coordinate.models.Coordinate coordinate) {
        return toBackendLib(transform(coordinate.toJts(), coordinate.srid()));
    }

    /** Transforms the given coordinate into the current spatial reference system. */
    public Coordinate transform(final Coordinate coordinate, final int sourceSRID) {
        if (sourceSRID == srid) {
            return coordinate; // No transform need, already in correct spatial reference system
        }
        try {
            final var withHeight = !Double.isNaN(coordinate.getZ());
            return JTS.transform(coordinate, null, findMathTransform(sourceSRID, withHeight));
        } catch (final TransformException e) {
            throw new WrappedException(
                    e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    public <T extends ch.ubique.backend.geo.coordinate.models.Coordinate> BoundingBox<C> transform(
            final BoundingBox<T> boundingBox) {
        return new BoundingBox<>(transform(boundingBox.min()), transform(boundingBox.max()));
    }
}
