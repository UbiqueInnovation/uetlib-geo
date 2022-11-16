package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.eclipse.emf.common.util.WrappedException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.PassThroughTransform;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;
import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getSimpleGeometriesIterator;
import static org.locationtech.jts.geom.Coordinate.NULL_ORDINATE;

public class JtsConverter<C extends ch.ubique.backend.geo.coordinate.models.Coordinate> {

    private final int srid;
    private final CoordinateReferenceSystem crs;
    private final GeometryFactory geometryFactory;
    private final Function<C, Coordinate> toJtsMapper;
    private final Function<Coordinate, C> fromJtsMapper;

    private JtsConverter(int srid, Function<C, Coordinate> toJtsMapper, Function<Coordinate, C> fromJtsMapper) {
        this.srid = srid;
        this.toJtsMapper = toJtsMapper;
        this.fromJtsMapper = fromJtsMapper;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        this.crs = getCrs(srid);
    }
    
    public static final JtsConverter<WGS84Coordinate> WGS84 = new JtsConverter<>(4326,
            c -> new Coordinate(c.getLongitude(), c.getLatitude(), c.getElevation().orElse(NULL_ORDINATE)),
            c -> new WGS84Coordinate(c.y, c.x, nullIfNaN(c.getZ()))
    );

    public static final JtsConverter<WebMercatorCoordinate> WEB_MERCATOR = new JtsConverter<>(3857,
            c -> new Coordinate(c.getX(), c.getY(), c.getElevation().orElse(NULL_ORDINATE)),
            c -> new WebMercatorCoordinate(c.x, c.y, nullIfNaN(c.getZ()))
    );

    public static final JtsConverter<LV95Coordinate> LV95 = new JtsConverter<>(2056,
            c -> new Coordinate(c.getEast(), c.getNorth(), c.getElevation().orElse(NULL_ORDINATE)),
            c -> new LV95Coordinate(c.x, c.y, nullIfNaN(c.getZ()))
    );

    public static final JtsConverter<LV03Coordinate> LV03 = new JtsConverter<>(21781,
            c -> new Coordinate(c.getEast(), c.getNorth(), c.getElevation().orElse(NULL_ORDINATE)),
            c -> new LV03Coordinate(c.x, c.y, nullIfNaN(c.getZ()))
    );

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
    public LineString toJtsLineString(Collection<C> coordinates) {
        return geometryFactory.createLineString(toJts(coordinates));
    }

    @NotNull
    public Polygon toJtsPolygon(Collection<C> exteriorRing) {
        return toJtsPolygon(exteriorRing, Set.of());
    }

    @NotNull
    public Polygon toJtsPolygon(Collection<C> exteriorRing, Collection<List<C>> interiorRings) {
        return geometryFactory.createPolygon(
                toJtsLinearRing(exteriorRing),
                toJtsLinearRings(interiorRings));
    }

    @NotNull
    public Coordinate[] toJts(Collection<C> coordinates) {
        return coordinates.stream().map(this::toJts).toArray(Coordinate[]::new);
    }

    @NotNull
    public LinearRing toJtsLinearRing(Collection<C> coordinates) {
        return geometryFactory.createLinearRing(toJts(coordinates));
    }

    @NotNull
    public LinearRing[] toJtsLinearRings(Collection<List<C>> coordinates) {
        return coordinates.stream().map(this::toJtsLinearRing).toArray(LinearRing[]::new);
    }

    /**
     * Transforms the given geometry into the current spatial reference system.
     */
    public Geometry transform(final Geometry geometry) {
        if (geometry.getSRID() == srid) {
            return geometry; // No transform need, already in correct spatial reference system
        }

        try {
            final var transformedGeometry =
                    JTS.transform(geometry, findMathTransform(geometry.getSRID()));

            // JTS.transform doesnt update srid
            transformedGeometry.setSRID(srid);
            getSimpleGeometriesIterator(transformedGeometry).forEachRemaining(g -> g.setSRID(srid));

            return transformedGeometry;
        } catch (TransformException e) {
            throw new WrappedException(e); // rethrow as runtime exception. no way for caller to handle this

        }
    }

    /**
     * Transforms the given coordinate into the current spatial reference system.
     */
    public Coordinate transform(Coordinate coordinate, int sourceSRID) {
        if (sourceSRID == srid) {
            return coordinate; // No transform need, already in correct spatial reference system
        }
        try {
            return JTS.transform(coordinate, null, findMathTransform(sourceSRID));
        } catch (TransformException e) {
            throw new WrappedException(e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    public C transform(ch.ubique.backend.geo.coordinate.models.Coordinate coordinate) {
        return toBackendLib(transform(coordinate.toJts(), coordinate.srid()));
    }

    public MathTransform findMathTransform(int sourceSRID) {
        try {
            final var baseTransform = CRS.findMathTransform(getCrs(sourceSRID), crs);

            if (baseTransform.getTargetDimensions() <= 2) {
                // This ensures, that the height of the coordinate is passed through
                // if the base transform only transforms the first two coordinates
                return PassThroughTransform.create(0, baseTransform, 1);
            }

            return baseTransform;
        } catch (FactoryException e) {
            throw new WrappedException(e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    private static Double nullIfNaN(Double value){
        if (value != null && value.isNaN()) {
            return null;
        }
        return value;
    }
}
