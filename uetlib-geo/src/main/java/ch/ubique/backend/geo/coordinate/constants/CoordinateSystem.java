package ch.ubique.backend.geo.coordinate.constants;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import ch.ubique.backend.geo.coordinate.models.Coordinate;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.function.Function;

public final class CoordinateSystem<C extends Coordinate> {

    private final int srid;
    private final C minCoordinate;
    private final C maxCoordinate;
    private final Function<C, org.locationtech.jts.geom.Coordinate> toJtsMapper;
    private final Function<org.locationtech.jts.geom.Coordinate, C> fromJtsMapper;

    // Derived fields
    private final Class<C> coordinateClass;
    private final org.locationtech.jts.geom.Coordinate minJtsCoordinate;
    private final org.locationtech.jts.geom.Coordinate maxJtsCoordinate;
    private final ReferencedEnvelope bounds;
    private final GeometryFactory geometryFactory;
    private final CoordinateReferenceSystem crs;

    CoordinateSystem(
            final int srid,
            final Function<C, org.locationtech.jts.geom.Coordinate> toJtsMapper,
            final Function<org.locationtech.jts.geom.Coordinate, C> fromJtsMapper,
            final C minCoordinate,
            final C maxCoordinate) {
        this.srid = srid;
        this.minCoordinate = minCoordinate;
        this.maxCoordinate = maxCoordinate;
        this.toJtsMapper = toJtsMapper;
        this.fromJtsMapper = fromJtsMapper;
        //noinspection unchecked
        this.coordinateClass = (Class<C>) minCoordinate.getClass();
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        this.crs = getCrs(srid);
        this.minJtsCoordinate = toJtsMapper.apply(minCoordinate);
        this.maxJtsCoordinate = toJtsMapper.apply(maxCoordinate);
        this.bounds = new ReferencedEnvelope(new Envelope(minJtsCoordinate, maxJtsCoordinate), crs);
    }

    public int srid() {
        return srid;
    }

    public CoordinateReferenceSystem crs() {
        return crs;
    }

    public C minCoordinate() {
        return minCoordinate;
    }

    public C maxCoordinate() {
        return maxCoordinate;
    }

    public org.locationtech.jts.geom.Coordinate minJtsCoordinate() {
        return minJtsCoordinate;
    }

    public org.locationtech.jts.geom.Coordinate maxJtsCoordinate() {
        return maxJtsCoordinate;
    }

    public GeometryFactory geometryFactory() {
        return geometryFactory;
    }

    public org.locationtech.jts.geom.Coordinate toJts(final C coordinate) {
        return toJtsMapper.apply(coordinate);
    }

    public C fromJts(final org.locationtech.jts.geom.Coordinate coordinate) {
        return fromJtsMapper.apply(coordinate);
    }

    public ReferencedEnvelope bounds() {
        return bounds;
    }

    public Class<C> coordinateClass() {
        return coordinateClass;
    }
}
