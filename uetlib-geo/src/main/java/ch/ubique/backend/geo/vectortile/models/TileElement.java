package ch.ubique.backend.geo.vectortile.models;

import static ch.ubique.backend.geo.coordinate.util.PointConverter.lv95toPoints;
import static ch.ubique.backend.geo.coordinate.util.PointConverter.toLv95Coordinate;
import static ch.ubique.backend.geo.coordinate.util.PointConverter.toPoint;
import static java.lang.Math.abs;
import static java.lang.Math.toRadians;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.geometry.models.BoundingBox;
import ch.ubique.backend.geo.geometry.models.BoundingBox2d;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Point3d;
import ch.ubique.backend.geo.geometry.utils.BoundingBoxHelper;
import ch.ubique.backend.geo.geometry.utils.GeometryHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TileElement {

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));

    private TileElementType elementType = TileElementType.INFERRED;

    // Abstract methods

    public abstract long getId();

    public abstract String getLayer();

    /** Return a score for this element. The higher, the more important. */
    public abstract double getScore();

    public abstract Map<String, Object> getAttributes();

    public abstract List<LV95Coordinate> getCoordinates();

    public abstract void setCoordinates(List<LV95Coordinate> coordinates);

    public abstract List<List<LV95Coordinate>> getHoles();

    public abstract void setHoles(List<List<LV95Coordinate>> holes);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Common implementation

    public boolean showElement(int zoomLevel) {
        return true;
    }

    public BoundingBox<Point2d> getBoundingBox() {
        return BoundingBoxHelper.getBoundingBox(lv95toPoints(Point2d.class, getCoordinates()));
    }

    public GeometryWithAttributes getGeometry(BoundingBox2d tileBoundingBox, int extend) {
        var type = inferGeometryType();

        switch (type) {
            case POINT:
                return getPoint(getCoordinates().get(0), tileBoundingBox, extend);
            case LINE:
                return getLine(tileBoundingBox, extend);
            case POLYGON:
                return getPolygon(tileBoundingBox, extend);
            default:
                throw new RuntimeException("TileElementType " + type + " not implemented");
        }
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
        double minDistance = zoomLevel.getResolution() / 10;

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
                            .collect(Collectors.toList()));
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
                            .collect(Collectors.toList()));
        }

        // Calculate size reduction
        return 1. * getCoordinates().size() / initialSize;
    }

    private boolean is3d() {
        return getCoordinates().stream().allMatch(c -> c.getElevation() != null);
    }

    private GeometryWithAttributes getPoint(
            LV95Coordinate position, BoundingBox2d tileBoundingBox, int extend) {
        Coordinate coordinate =
                getCoordinateOnTile(toPoint(Point2d.class, position), tileBoundingBox, extend);
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

    private GeometryWithAttributes getLine(BoundingBox2d tileBoundingBox, int extend) {
        var points = lv95toPoints(Point2d.class, getCoordinates());
        Coordinate[] coordinates = toTileCoordinateArray(points, tileBoundingBox, extend);
        if (coordinates.length == 1) {
            return getPoint(coordinates[0], getCoordinates().get(0).getElevation());
        }
        Geometry geometry = geometryFactory.createLineString(coordinates);
        Map<String, Object> attributes = getAttributes();
        addElevationAttributes(getCoordinates(), attributes);
        return new GeometryWithAttributes(geometry, attributes);
    }

    private GeometryWithAttributes getPolygon(BoundingBox2d tileBoundingBox, int extend) {
        var points = lv95toPoints(Point2d.class, getCoordinates());
        Coordinate[] shell = toTileCoordinateArray(points, tileBoundingBox, extend);
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
                                                            lv95toPoints(Point2d.class, h),
                                                            tileBoundingBox,
                                                            extend))
                                    .filter(h -> h.length > 2)
                                    .map(h -> geometryFactory.createLinearRing(h))
                                    .toArray(LinearRing[]::new));
        }

        if (!geometry.isValid()) {
            logger.debug(
                    "Geometry  {}  not valid, try to created fixed version", this.getAttributes());
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

    private Coordinate getCoordinateOnTile(Point2d point, BoundingBox2d boundingBox, int extend) {
        double x =
                Math.round(
                        (point.getX() - boundingBox.getMin().getX())
                                / boundingBox.getWidth()
                                * extend);
        double y =
                Math.round(
                        (point.getY() - boundingBox.getMin().getY())
                                / boundingBox.getHeight()
                                * extend);
        return new Coordinate(x, y);
    }

    private Coordinate[] toTileCoordinateArray(
            List<Point2d> points, BoundingBox2d boundingBox, int extend) {
        var rawPoints =
                points.stream()
                        .map(coordinate -> getCoordinateOnTile(coordinate, boundingBox, extend))
                        .collect(Collectors.toList());

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

    /**
     * Get / create a valid version of the geometry given. If the geometry is a polygon or multi
     * polygon, self intersections / inconsistencies are fixed. Otherwise the geometry is returned.
     *
     * @return a geometry
     */
    private Geometry validate(Geometry geom) {
        if (geom instanceof Polygon) {
            if (geom.isValid()) {
                geom
                        .normalize(); // validate does not pick up rings in the wrong order - this
                                      // will fix that
                return geom; // If the polygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((Polygon) geom, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        } else if (geom instanceof MultiPolygon) {
            if (geom.isValid()) {
                geom
                        .normalize(); // validate does not pick up rings in the wrong order - this
                                      // will fix that
                return geom; // If the multipolygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            for (int n = geom.getNumGeometries(); n-- > 0; ) {
                addPolygon((Polygon) geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
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
     * @param factory factory to generate MultiPolygon if required
     * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon
     *     containing all polygons otherwise
     */
    private Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory) {
        switch (polygons.size()) {
            case 0:
                return null; // No valid polygons!
            case 1:
                return polygons.iterator().next(); // single polygon - no need to wrap
            default:
                // polygons may still overlap! Need to sym difference them
                Iterator<Polygon> iter = polygons.iterator();
                Geometry ret = iter.next();
                while (iter.hasNext()) {
                    if (ret instanceof GeometryCollection) {
                        // symDifference can't handle geometry collections. only keep polygons
                        List<Geometry> symDifPolygons = new ArrayList<>();
                        for (int i = 0; i < ret.getNumGeometries(); i++) {
                            final Geometry geometry = ret.getGeometryN(i);
                            if (geometry instanceof Polygon) {
                                symDifPolygons.add(geometry);
                            }
                        }
                        if (symDifPolygons.size() == 1) {
                            ret = symDifPolygons.get(0);
                        } else {
                            ret =
                                    new MultiPolygon(
                                            symDifPolygons.stream().toArray(Polygon[]::new),
                                            geometryFactory);
                        }
                    }
                    ret = ret.symDifference(iter.next());
                }
                return ret;
        }
    }
}
