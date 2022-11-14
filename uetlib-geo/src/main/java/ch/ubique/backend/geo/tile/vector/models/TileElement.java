package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Point3d;
import ch.ubique.backend.geo.geometry.utils.BoundingBoxHelper;
import ch.ubique.backend.geo.geometry.utils.GeometryHelper;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static ch.ubique.backend.geo.coordinate.util.PointConverter.*;
import static java.lang.Math.abs;
import static java.lang.Math.toRadians;

/**
 * @deprecated This implementation does not work with JTS geometries all the way,
 * which introduces some weird behaviour, and bugs. Please use the
 * {@link VectorTileElement} interface directly.
 */
@Deprecated(since = "0.7.11", forRemoval = true)
public abstract class TileElement implements VectorTileElementWithTagsAndMetadata<Map<String, Object>, Object> {

    protected abstract long getId();

    protected abstract String getLayer();

    protected abstract double getScore();

    protected abstract Object getMetadata();

    protected abstract Map<String, Object> getAttributes();

    public abstract List<LV95Coordinate> getCoordinates();

    public abstract void setCoordinates(List<LV95Coordinate> coordinates);

    public abstract List<List<LV95Coordinate>> getHoles();

    public abstract void setHoles(List<List<LV95Coordinate>> holes);

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED), 2056);

    private TileElementType elementType = TileElementType.INFERRED;

    /**
     * this set controls which zoom levels the element is added to. if null (default) the element is
     * included in all zoom levels
     */
    private Set<Integer> zoomLevels;

    public void setZoomLevels(Set<Integer> zoomLevels) {
        this.zoomLevels = zoomLevels;
    }

    public void includeZoomLevel(Integer zoomLevel) {
        if (zoomLevels == null) {
            zoomLevels = new HashSet<>();
        }
        zoomLevels.add(zoomLevel);
    }

    public void excludeZoomLevel(Integer zoomLevel) {
        if (zoomLevels != null) {
            zoomLevels.remove(zoomLevel);
        }
    }

    public boolean isVisibleAtZoomLevel(ZoomLevel zoomLevel) {
        return isVisibleAtZoomLevel(zoomLevel.level());
    }

    public boolean isVisibleAtZoomLevel(Integer zoomLevel) {
        return zoomLevels == null || zoomLevels.contains(zoomLevel);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Common implementation

    @Override
    public boolean showElement(int zoomLevel) {
        return isVisibleAtZoomLevel(zoomLevel);
    }

    @Override
    public long id() {
        return getId();
    }

    @Override
    public String layer() {
        return getLayer();
    }

    @Override
    public double score() {
        return getScore();
    }

    @Override
    public Object metadata() {
        return getMetadata();
    }

    @Override
    public final Envelope boundingBox() {
        return BoundingBoxHelper.getEnvelope(BoundingBoxHelper.getBoundingBox(lv95toPoints(Point2d.class, getCoordinates())));
    }

    @Override
    public final Map<String, Object> tags() {
        return Optional.ofNullable(getGeometryWithAttributes())
                .map(GeometryWithAttributes::attributes)
                .orElse(null);
    }

    @Override
    public final Geometry geometry() {
        return Optional.ofNullable(getGeometryWithAttributes())
                .map(GeometryWithAttributes::geometry)
                .orElse(null);
    }

    public GeometryWithAttributes getGeometryWithAttributes() {
        var type = inferGeometryType();

        return switch (type) {
            case POINT -> getPoint(getCoordinates().get(0));
            case LINE -> getLine();
            case POLYGON -> getPolygon();
            default -> throw new UnsupportedOperationException("TileElementType " + type + " not implemented");
        };
    }

    private TileElementType inferGeometryType() {
        var type = this.elementType;
        if (type != TileElementType.INFERRED) {
            return type;
        }

        if (getCoordinates().size() == 1) {
            return TileElementType.POINT;
        }

        // Last point equals first point => closed
        boolean isPolygon =
                getCoordinates().get(0).equals(getCoordinates().get(getCoordinates().size() - 1));
        return isPolygon ? TileElementType.POLYGON : TileElementType.LINE;
    }

    public double simplify(ZoomLevel zoomLevel) {
        // Do not simplify points and lines with only two points
        if (getCoordinates().size() <= 2) {
            return 1;
        }

        double minAngle = toRadians(5);
        double minDistance = zoomLevel.tileWidth() / 1000;

        int initialSize = getCoordinates().size();

        // Simplify
        if (is3d()) {
            List<Point3d> simplified =
                    GeometryHelper.simplify(
                            lv95toPoints(Point3d.class, getCoordinates()), minAngle, minDistance);
            setCoordinates(toLv95Coordinate(simplified));

            setHoles(
                    getHoles().stream()
                            .map(
                                    h ->
                                            toLv95Coordinate(
                                                    GeometryHelper.simplify(
                                                            lv95toPoints(Point3d.class, h),
                                                            minAngle,
                                                            minDistance)))
                            .toList());
        } else {
            List<Point2d> simplified =
                    GeometryHelper.simplify(
                            lv95toPoints(Point2d.class, getCoordinates()), minAngle, minDistance);
            setCoordinates(toLv95Coordinate(simplified));

            setHoles(
                    getHoles().stream()
                            .map(
                                    h ->
                                            toLv95Coordinate(
                                                    GeometryHelper.simplify(
                                                            lv95toPoints(Point2d.class, h),
                                                            minAngle,
                                                            minDistance)))
                            .toList());
        }

        // Calculate size reduction
        return 1. * getCoordinates().size() / initialSize;
    }

    private boolean is3d() {
        return getCoordinates().stream().allMatch(c -> c.getElevation() != null);
    }

    private GeometryWithAttributes getPoint(LV95Coordinate position) {
        Coordinate coordinate = getCoordinate(toPoint(Point2d.class, position));
        return getPoint(coordinate, position.getElevation());
    }

    private GeometryWithAttributes getPoint(Coordinate coordinate, Double elevation) {
        Geometry geometry = geometryFactory.createPoint(coordinate);
        Map<String, Object> attributes = getAttributes();
        if (elevation != null) {
            attributes.put("elevation", elevation);
        }
        return new GeometryWithAttributes(geometry, attributes);
    }

    private GeometryWithAttributes getLine() {
        var points = lv95toPoints(Point2d.class, getCoordinates());
        Coordinate[] coordinates =
                toTileCoordinateArray(points);
        if (coordinates.length == 1) {
            return getPoint(coordinates[0], getCoordinates().get(0).getElevation());
        }
        Geometry geometry = geometryFactory.createLineString(coordinates);
        Map<String, Object> attributes = getAttributes();
        addElevationAttributes(getCoordinates(), attributes);
        return new GeometryWithAttributes(geometry, attributes);
    }

    private GeometryWithAttributes getPolygon() {
        var points = lv95toPoints(Point2d.class, getCoordinates());
        Coordinate[] shell = toTileCoordinateArray(points);
        Geometry geometry;
        if (shell.length == 1
                || (shell.length == 2 && shell[0].equals(shell[1]))) { // Point. Some points come as
            // linestring with two equal
            // coordinates :(
            geometry = geometryFactory.createPoint(shell[0]);
        } else if (shell.length < 3
                || !shell[0].equals(shell[shell.length - 1])
                || (shell.length == 3
                        && shell[0].equals(
                                shell[2]))) { // Line. Some lines come with tree coordinates,
            // where start and end are equal :(
            geometry = geometryFactory.createLineString(shell);
        } else {
            geometry =
                    geometryFactory.createPolygon(
                            geometryFactory.createLinearRing(shell),
                            getHoles().stream()
                                    .map(
                                            h ->
                                                    toTileCoordinateArray(
                                                            lv95toPoints(Point2d.class, h)))
                                    .filter(h -> h.length > 2)
                                    .map(geometryFactory::createLinearRing)
                                    .toArray(LinearRing[]::new));
        }

        if (!geometry.isValid()) {
            logger.debug(
                    "Geometry  {}  not valid, try to created fixed version",
                    this.getAttributes());
            geometry = validate(geometry);
        }

        Map<String, Object> attributes = getAttributes();
        addElevationAttributes(getCoordinates(), attributes);
        return new GeometryWithAttributes(geometry, attributes);
    }

    public TileElementType getElementType() {
        return elementType;
    }

    public void setElementType(TileElementType type) {
        this.elementType = type;
    }

    private void addElevationAttributes(
            List<LV95Coordinate> points, Map<String, Object> attributes) {
        List<Double> slopes = calculateSlopes(points);
        Double maxSlope = null;
        for (int i = 0; i < points.size(); ++i) {
            Double elevation = points.get(i).getElevation();
            Double slope = slopes.get(i);
            if (elevation != null) {
                attributes.put("elevation_" + i, elevation);
            }
            if (slope != null) {
                attributes.put("slope_" + i, slope);
                if (maxSlope == null || slope > maxSlope) {
                    maxSlope = slope;
                }
            }
        }
        if (maxSlope != null) {
            attributes.put("max_slope", maxSlope);
        }
    }

    private List<Double> calculateSlopes(List<LV95Coordinate> points) {
        List<Double> slopes = new ArrayList<>(points.size());

        if (points.size() <= 1) {
            return slopes;
        }

        // Calculate slope for points
        slopes.add(calculateSlope(points.get(0), points.get(1)));
        for (int i = 1; i < points.size() - 1; ++i) {
            Double slope = calculateSlope(points.get(i - 1), points.get(i + 1));
            slopes.add(slope);
        }
        slopes.add(calculateSlope(points.get(points.size() - 2), points.get(points.size() - 1)));
        return slopes;
    }

    private Double calculateSlope(LV95Coordinate a, LV95Coordinate b) {
        if (a.getElevation() == null || b.getElevation() == 0) {
            return null;
        }
        return abs(a.getElevation() - b.getElevation()) / a.distanceTo(b);
    }


    private Coordinate[] toTileCoordinateArray(List<Point2d> points) {
        var rawPoints =
                points.stream()
                        .map(this::getCoordinate)
                        .toList();

        if (rawPoints.isEmpty()) {
            return new Coordinate[0];
        }

        // Remove duplicates
        var finalPoints = new ArrayList<Coordinate>();
        finalPoints.add(rawPoints.get(0));

        for (var i = 1; i < rawPoints.size(); ++i) {
            var lastPoint = rawPoints.get(i - 1);
            var currentPoint = rawPoints.get(i);

            if (!currentPoint.equals2D(lastPoint)) {
                finalPoints.add(currentPoint);
            }
        }

        return finalPoints.toArray((Coordinate[]::new));
    }

    private Coordinate getCoordinate(Point2d point2d) {
        return new Coordinate(point2d.getX(), point2d.getY());
    }

    /**
     * Get / create a valid version of the geometry given. If the geometry is a polygon or multi
     * polygon, self intersections / inconsistencies are fixed. Otherwise the geometry is returned.
     *
     * @return a geometry
     */
    @SuppressWarnings("unchecked")
    private Geometry validate(Geometry geom) {
        if (geom instanceof Polygon polygon) {
            if (polygon.isValid()) {
                polygon.normalize(); // validate does not pick up rings in the wrong order - this
                // will fix that
                return polygon; // If the polygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon(polygon, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons());
        } else if (geom instanceof MultiPolygon multiPolygon) {
            if (multiPolygon.isValid()) {
                multiPolygon
                        .normalize(); // validate does not pick up rings in the wrong order - this
                // will fix that
                return multiPolygon; // If the multipolygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            for (int n = multiPolygon.getNumGeometries(); n-- > 0; ) {
                addPolygon((Polygon) multiPolygon.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons());
        } else {
            return geom; // In my case, I only care about polygon / multipolygon geometries
        }
    }

    /**
     * Add all line strings from the polygon given to the polygonizer given
     *
     * @param polygon polygon from which to extract line strings
     * @param polygonizer polygonizer
     */
    private void addPolygon(Polygon polygon, Polygonizer polygonizer) {
        addLineString(polygon.getExteriorRing(), polygonizer);
        for (int n = polygon.getNumInteriorRing(); n-- > 0; ) {
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    /**
     * Add the linestring given to the polygonizer
     *
     * @param lineString line string
     * @param polygonizer polygonizer
     */
    private void addLineString(LineString lineString, Polygonizer polygonizer) {

        if (lineString
                instanceof
                LinearRing) { // LinearRings are treated differently to line strings : we need a
            // LineString NOT a LinearRing
            lineString =
                    lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }

        // unioning the linestring with the point makes any self intersections explicit.
        Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        Geometry toAdd = lineString.union(point);

        // Add result to polygonizer
        polygonizer.add(toAdd);
    }

    /**
     * Get a geometry from a collection of polygons.
     *
     * @param polygons collection
     * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon
     *     containing all polygons otherwise
     */
    private Geometry toPolygonGeometry(Collection<Polygon> polygons) {
        return switch (polygons.size()) {
            case 0 -> null; // No valid polygons!
            case 1 -> polygons.iterator().next(); // single polygon - no need to wrap
            default -> {
                // polygons may still overlap! Need to sym difference them
                Iterator<Polygon> iter = polygons.iterator();
                Geometry ret = iter.next();
                while (iter.hasNext()) {
                    if (ret instanceof GeometryCollection) {
                        // symDifference can't handle geometry collections. only keep polygons
                        List<Polygon> symDifPolygons = new ArrayList<>();
                        for (int i = 0; i < ret.getNumGeometries(); i++) {
                            final Geometry geometry = ret.getGeometryN(i);
                            if (geometry instanceof Polygon polygon) {
                                symDifPolygons.add(polygon);
                            }
                        }
                        if (symDifPolygons.size() == 1) {
                            ret = symDifPolygons.get(0);
                        } else {
                            ret =
                                    new MultiPolygon(
                                            symDifPolygons.toArray(Polygon[]::new),
                                            geometryFactory);
                        }
                    }
                    ret = ret.symDifference(iter.next());
                }
                yield ret;
            }
        };
    }
}
