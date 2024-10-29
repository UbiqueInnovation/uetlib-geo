package ch.ubique.backend.geo.tile.vector.mapbox.decoding;

import ch.ubique.backend.geo.tile.vector.mapbox.commands.*;
import ch.ubique.backend.geo.tile.vector.mapbox.helpers.GeometryCursor;
import ch.ubique.backend.geo.vectortile.protobuf.Tile.GeomType;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class GeometryDecoder {

    private GeometryDecoder() {}

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(1));

    public static Geometry decode(final GeomType type, final List<Command> commands) {
        return switch (type) {
            case POINT -> decodePoint(commands);
            case LINESTRING -> decodeLinestring(commands);
            case POLYGON -> decodePolygon(commands);
            case UNKNOWN ->
                    throw new IllegalArgumentException("Geometry type must not be UNKNOWN.");
        };
    }

    private static Geometry decodePoint(final List<Command> commands) {
        final var cursor = new GeometryCursor();
        final var coordinates = toArray(commands.stream().flatMap(cursor::move));
        if (coordinates.length == 1) {
            return GEOMETRY_FACTORY.createPoint(coordinates[0]);
        }
        return GEOMETRY_FACTORY.createMultiPointFromCoords(coordinates);
    }

    private static Geometry decodeLinestring(final List<Command> commands) {
        final var lineStrings =
                decodeCoordinateSequences(commands)
                        .map(GEOMETRY_FACTORY::createLineString)
                        .toArray(LineString[]::new);
        if (lineStrings.length == 1) {
            return lineStrings[0];
        }
        return GEOMETRY_FACTORY.createMultiLineString(lineStrings);
    }

    private static Geometry decodePolygon(final List<Command> commands) {
        final var rings =
                decodeCoordinateSequences(commands).map(GEOMETRY_FACTORY::createLinearRing);

        final var polygons =
                groupLinearRings(rings).stream()
                        .map(GeometryDecoder::createPolygon)
                        .toArray(Polygon[]::new);

        if (polygons.length == 1) {
            return polygons[0];
        }
        return GEOMETRY_FACTORY.createMultiPolygon(polygons);
    }

    private static Collection<List<LinearRing>> groupLinearRings(final Stream<LinearRing> rings) {
        final var groupedRings = new ArrayList<List<LinearRing>>();
        final var ringIterator = rings.iterator();
        while (ringIterator.hasNext()) {
            final var ring = ringIterator.next();
            final var isExterior = Area.ofRingSigned(ring.getCoordinates()) < 0;
            if (isExterior) {
                groupedRings.add(new ArrayList<>(List.of(ring)));
            } else {
                groupedRings.getLast().add(ring);
            }
        }
        return groupedRings;
    }

    private static Stream<Coordinate[]> decodeCoordinateSequences(final List<Command> commands) {
        final var cursor = new GeometryCursor();
        final var coordinateSequences = new ArrayList<List<Coordinate>>();
        for (final var command : commands) {
            final var coordinates = cursor.move(command).toList();
            if (command instanceof MoveToCommand) {
                coordinateSequences.add(new ArrayList<>(List.of(coordinates.getLast())));
            }
            if (command instanceof LineToCommand) {
                coordinateSequences.getLast().addAll(coordinates);
            }
            if (command instanceof ClosePathCommand()) {
                coordinateSequences.getLast().add(coordinateSequences.getLast().getFirst());
            }
        }
        return coordinateSequences.stream().map(list -> list.toArray(Coordinate[]::new));
    }

    private static Polygon createPolygon(final List<LinearRing> rings) {
        final var shell = rings.getFirst();
        final var holes = rings.subList(1, rings.size()).toArray(LinearRing[]::new);
        return GEOMETRY_FACTORY.createPolygon(shell, holes);
    }

    private static Coordinate[] toArray(final Stream<Coordinate> coordinates) {
        return coordinates.toArray(Coordinate[]::new);
    }
}
