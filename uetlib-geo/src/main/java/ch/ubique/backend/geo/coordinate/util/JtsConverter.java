package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.locationtech.jts.geom.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class JtsConverter<C extends ch.ubique.backend.geo.coordinate.models.Coordinate> {

    private final GeometryFactory geometryFactory;
    private final Function<C, Coordinate> toJtsMapper;
    private final Function<Coordinate, C> fromJtsMapper;

    private JtsConverter(int srid, Function<C, Coordinate> toJtsMapper, Function<Coordinate, C> fromJtsMapper) {
        geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        this.toJtsMapper = toJtsMapper;
        this.fromJtsMapper = fromJtsMapper;
    }
    
    public static final JtsConverter<WGS84Coordinate> WGS84 = new JtsConverter<>(4326,
            c -> new Coordinate(c.getLongitude(), c.getLatitude()),
            c -> new WGS84Coordinate(c.y, c.x)
    );

    public static final JtsConverter<WebMercatorCoordinate> WEB_MERCATOR = new JtsConverter<>(3857,
            c -> new Coordinate(c.getX(), c.getY()),
            c -> new WebMercatorCoordinate(c.x, c.y)
    );

    public static final JtsConverter<LV95Coordinate> LV95 = new JtsConverter<>(2056,
            c -> new Coordinate(c.getEast(), c.getNorth()),
            c -> new LV95Coordinate(c.x, c.y)
    );

    public static final JtsConverter<LV03Coordinate> LV03 = new JtsConverter<>(21781,
            c -> new Coordinate(c.getEast(), c.getNorth()),
            c -> new LV03Coordinate(c.x, c.y)
    );


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


}
