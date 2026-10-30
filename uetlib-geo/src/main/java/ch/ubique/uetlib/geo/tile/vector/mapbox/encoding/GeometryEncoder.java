package ch.ubique.uetlib.geo.tile.vector.mapbox.encoding;

import ch.ubique.uetlib.geo.coordinate.util.JtsUtil;
import ch.ubique.uetlib.geo.tile.vector.mapbox.commands.*;
import ch.ubique.uetlib.geo.vectortile.protobuf.Tile;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;

public class GeometryEncoder {

    // Current position of the cursor
    private final DeltaEncoder deltaEncoder = new DeltaEncoder(false);
    private final Geometry geometry;

    public GeometryEncoder(final Geometry geometry) {
        this.geometry = geometry;
    }

    public List<Command> encode() {
        return switch (geometry) {
            case final Point point -> encode(point);
            case final MultiPoint multiPoint -> encode(multiPoint);
            case final LineString lineString -> encode(lineString);
            case final MultiLineString multiLineString -> encode(multiLineString);
            case final Polygon polygon -> encode(polygon);
            case final MultiPolygon multiPolygon -> encode(multiPolygon);
            default -> throw new IllegalArgumentException("Unexpected geometry type: " + geometry);
        };
    }

    @SuppressWarnings("java:S1481")
    public Tile.GeomType getGeomType() {
        return switch (geometry) {
            case final Point ignored -> Tile.GeomType.POINT;
            case final MultiPoint ignored -> Tile.GeomType.POINT;
            case final LineString ignored -> Tile.GeomType.LINESTRING;
            case final MultiLineString ignored -> Tile.GeomType.LINESTRING;
            case final Polygon ignored -> Tile.GeomType.POLYGON;
            case final MultiPolygon ignored -> Tile.GeomType.POLYGON;
            default -> throw new IllegalArgumentException("Unexpected geometry type: " + geometry);
        };
    }

    private List<Command> encode(final Point point) {
        return List.of(new MoveToCommand(deltaEncoder.visitCoordinates(point.getCoordinates())));
    }

    private List<Command> encode(final MultiPoint multiPoint) {
        return List.of(
                new MoveToCommand(deltaEncoder.visitCoordinates(multiPoint.getCoordinates())));
    }

    private List<Command> encode(final LineString lineString) {
        final var deltas = deltaEncoder.visitCoordinates(lineString.getCoordinates());
        final var moveDelta = deltas.subList(0, 1); // Move to start
        final var lineDeltas = deltas.subList(1, deltas.size()); // Lines to next
        return List.of(new MoveToCommand(moveDelta), new LineToCommand(lineDeltas));
    }

    private List<Command> encode(final MultiLineString multiLineString) {
        return JtsUtil.getLinesStream(multiLineString)
                .map(this::encode)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<Command> encode(final Polygon polygon) {
        return Stream.concat(
                        encode(polygon.getExteriorRing(), true),
                        JtsUtil.getInteriorRingsStream(polygon).flatMap(r -> encode(r, false)))
                .toList();
    }

    private List<Command> encode(final MultiPolygon multiPolygon) {
        return JtsUtil.getPolygonsStream(multiPolygon)
                .map(this::encode)
                .flatMap(Collection::stream)
                .toList();
    }

    private Stream<Command> encode(final LinearRing linearRing, final boolean isExterior) {
        final var orientedAsExteriorRing = Area.ofRingSigned(linearRing.getCoordinates()) < 0;
        final var ringCoordinates = List.of(linearRing.getCoordinates());
        final var orientedRingCoordinates =
                (isExterior == orientedAsExteriorRing)
                        ? ringCoordinates
                        : ringCoordinates.reversed();

        // Exclude the last coordinate as it is equal to the first
        final var coordinates =
                orientedRingCoordinates.subList(0, orientedRingCoordinates.size() - 1);

        final var deltas = deltaEncoder.visitCoordinates(coordinates);
        return Stream.of(
                new MoveToCommand(deltas.subList(0, 1)), // Move to start
                new LineToCommand(deltas.subList(1, deltas.size())), // Lines to next
                new ClosePathCommand()); // Close path
    }
}
