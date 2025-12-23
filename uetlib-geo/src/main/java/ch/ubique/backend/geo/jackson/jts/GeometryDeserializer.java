/*
 * Copyright 2019-2025 52°North Spatial Information Research GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ubique.backend.geo.jackson.jts;

import jakarta.annotation.Nullable;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.Optional;

/**
 * {@link ValueDeserializer} for {@link Geometry}.
 *
 * @author Christian Autermann
 */
public class GeometryDeserializer extends ValueDeserializer<Geometry> {
    private static final int DEFAULT_SRID = 4326;
    private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = getDefaultGeometryFactory();
    private final GeometryFactory geometryFactory;

    /** Creates a new {@link GeometryDeserializer}. */
    public GeometryDeserializer() {
        this(null);
    }

    /**
     * Creates a new {@link GeometryDeserializer}.
     *
     * @param geometryFactory The {@link GeometryFactory} to use to construct geometries.
     */
    public GeometryDeserializer(@Nullable final GeometryFactory geometryFactory) {
        this.geometryFactory =
                Optional.ofNullable(geometryFactory).orElse(DEFAULT_GEOMETRY_FACTORY);
    }

    @Override
    public Geometry deserialize(final JsonParser p, final DeserializationContext context) {
        return deserializeGeometry(p.readValueAs(JsonNode.class), context);
    }

    private Geometry deserializeGeometry(final JsonNode node, final DeserializationContext context)
            throws DatabindException {
        final var typeName = node.get(Field.TYPE).asString();

        final var type =
                GeometryType.fromString(typeName)
                        .orElseThrow(() -> invalidGeometryType(context, typeName));

        return switch (type) {
            case POINT -> deserializePoint(node, context);
            case MULTI_POINT -> deserializeMultiPoint(node, context);
            case LINE_STRING -> deserializeLineString(node, context);
            case MULTI_LINE_STRING -> deserializeMultiLineString(node, context);
            case POLYGON -> deserializePolygon(node, context);
            case MULTI_POLYGON -> deserializeMultiPolygon(node, context);
            case GEOMETRY_COLLECTION -> deserializeGeometryCollection(node, context);
        };
    }

    private DatabindException invalidGeometryType(
            final DeserializationContext context, final String typeName) {
        return DatabindException.from(context, "Invalid geometry type: " + typeName);
    }

    private Point deserializePoint(final JsonNode node, final DeserializationContext context)
            throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        return geometryFactory.createPoint(deserializeCoordinate(coordinates, context));
    }

    private Polygon deserializePolygon(final JsonNode node, final DeserializationContext context)
            throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        return deserializeLinearRings(coordinates, context);
    }

    private MultiPolygon deserializeMultiPolygon(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        final var polygons = new Polygon[coordinates.size()];
        for (var i = 0; i != coordinates.size(); ++i) {
            polygons[i] = deserializeLinearRings(coordinates.get(i), context);
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    private MultiPoint deserializeMultiPoint(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        final var coords = deserializeCoordinates(coordinates, context);
        return geometryFactory.createMultiPointFromCoords(coords);
    }

    private GeometryCollection deserializeGeometryCollection(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var geometries = getArray(node, context, Field.GEOMETRIES);
        final var geom = new Geometry[geometries.size()];
        for (var i = 0; i != geometries.size(); ++i) {
            geom[i] = deserializeGeometry(geometries.get(i), context);
        }
        return geometryFactory.createGeometryCollection(geom);
    }

    private MultiLineString deserializeMultiLineString(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        final var lineStrings = lineStringsFromJson(coordinates, context);
        return geometryFactory.createMultiLineString(lineStrings);
    }

    private LineString[] lineStringsFromJson(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var strings = new LineString[node.size()];
        for (var i = 0; i != node.size(); ++i) {
            final var coordinates = deserializeCoordinates(node.get(i), context);
            strings[i] = geometryFactory.createLineString(coordinates);
        }
        return strings;
    }

    private LineString deserializeLineString(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var coordinates = getArray(node, context, Field.COORDINATES);
        final var coords = deserializeCoordinates(coordinates, context);
        return geometryFactory.createLineString(coords);
    }

    private Coordinate[] deserializeCoordinates(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var points = new Coordinate[node.size()];
        for (var i = 0; i != node.size(); ++i) {
            points[i] = deserializeCoordinate(node.get(i), context);
        }
        return points;
    }

    private Polygon deserializeLinearRings(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var shell = deserializeLinearRing(node.get(0), context);
        final var holes = new LinearRing[node.size() - 1];
        for (var i = 1; i < node.size(); ++i) {
            holes[i - 1] = deserializeLinearRing(node.get(i), context);
        }
        return geometryFactory.createPolygon(shell, holes);
    }

    private LinearRing deserializeLinearRing(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        final var coordinates = deserializeCoordinates(node, context);
        return geometryFactory.createLinearRing(coordinates);
    }

    private Coordinate deserializeCoordinate(
            final JsonNode node, final DeserializationContext context) throws DatabindException {
        if (node.size() < 2) {
            throw DatabindException.from(
                    context, String.format("Invalid number of ordinates: %d", node.size()));
        } else {
            final var x = getOrdinate(node, 0, context);
            final var y = getOrdinate(node, 1, context);
            if (node.size() < 3) {
                return new Coordinate(x, y);
            } else {
                final var z = getOrdinate(node, 2, context);
                return new Coordinate(x, y, z);
            }
        }
    }

    private JsonNode getArray(
            final JsonNode node, final DeserializationContext context, final String fieldName)
            throws DatabindException {
        final var coordinates = node.get(fieldName);
        if (coordinates != null && !coordinates.isArray()) {
            throw DatabindException.from(
                    context,
                    "Invalid coordinates, expecting an array but got: "
                            + coordinates.getNodeType().toString());
        }
        return coordinates;
    }

    private double getOrdinate(
            final JsonNode node, final int i, final DeserializationContext context)
            throws DatabindException {
        final var ordinate = node.get(i);
        if (!ordinate.isNumber()) {
            throw DatabindException.from(
                    context,
                    "Invalid coordinates, expecting numbers but got: "
                            + ordinate.getNodeType().toString());
        }
        return ordinate.asDouble();
    }

    private static GeometryFactory getDefaultGeometryFactory() {
        return new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), DEFAULT_SRID);
    }
}
