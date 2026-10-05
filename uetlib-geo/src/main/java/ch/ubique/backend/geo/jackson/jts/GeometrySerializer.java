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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * {@link ValueSerializer} for {@link Geometry}.
 *
 * @author Christian Autermann
 */
public class GeometrySerializer extends ValueSerializer<Geometry> {
    static final int DEFAULT_DECIMAL_PLACES = 8;
    private final NumberFormat decimalFormat;

    /**
     * Creates a new {@link GeometrySerializer}.
     *
     * @param decimalPlaces The number of decimal places for encoded coordinates.
     */
    public GeometrySerializer(final int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("decimalPlaces < 0");
        }
        this.decimalFormat = createNumberFormat(decimalPlaces);
    }

    private NumberFormat createNumberFormat(final int decimalPlaces) {
        final var format = NumberFormat.getInstance(Locale.ROOT);
        format.setRoundingMode(RoundingMode.HALF_UP);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(decimalPlaces);
        format.setGroupingUsed(false);
        return format;
    }

    @Override
    public Class<Geometry> handledType() {
        return Geometry.class;
    }

    @Override
    public void serialize(
            final Geometry geometry,
            final JsonGenerator generator,
            final SerializationContext provider) {
        switch (geometry) {
            case null -> generator.writeNull();
            case final Polygon polygon -> serialize(polygon, generator);
            case final Point point -> serialize(point, generator);
            case final MultiPoint multiPoint -> serialize(multiPoint, generator);
            case final MultiPolygon multiPolygon -> serialize(multiPolygon, generator);
            case final LineString lineString -> serialize(lineString, generator);
            case final MultiLineString multiLineString -> serialize(multiLineString, generator);
            case final GeometryCollection geometryCollection ->
                    serialize(geometryCollection, generator, provider);
            default ->
                    throw DatabindException.from(
                            generator,
                            String.format(
                                    "Geometry type %s is not supported.",
                                    geometry.getClass().getName()));
        }
    }

    private void serialize(
            final GeometryCollection value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();

        serializeType(GeometryType.GEOMETRY_COLLECTION, generator);

        generator.writeArrayPropertyStart(Field.GEOMETRIES.value());

        for (var i = 0; i != value.getNumGeometries(); ++i) {
            serialize(value.getGeometryN(i), generator, provider);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(final MultiPoint value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.MULTI_POINT, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES.value());

        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinate((Point) value.getGeometryN(i), generator);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(final MultiLineString value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.MULTI_LINE_STRING, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES.value());
        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinates((LineString) value.getGeometryN(i), generator);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(final MultiPolygon value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.MULTI_POLYGON, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES.value());
        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinates((Polygon) value.getGeometryN(i), generator);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(final Polygon value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.POLYGON, generator);
        generator.writeName(Field.COORDINATES.value());
        serializeCoordinates(value, generator);
        generator.writeEndObject();
    }

    private void serialize(final LineString value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.LINE_STRING, generator);
        generator.writeName(Field.COORDINATES.value());
        serializeCoordinates(value, generator);
        generator.writeEndObject();
    }

    private void serialize(final Point value, final JsonGenerator generator) {
        generator.writeStartObject();
        serializeType(GeometryType.POINT, generator);
        generator.writeName(Field.COORDINATES.value());
        serializeCoordinate(value, generator);
        generator.writeEndObject();
    }

    private void serializeType(final GeometryType type, final JsonGenerator generator) {
        generator.writeStringProperty(Field.TYPE.value(), type.toString());
    }

    private void serializeCoordinates(final Polygon value, final JsonGenerator generator) {
        generator.writeStartArray();
        if (!value.isEmpty()) {
            serializeCoordinates(value.getExteriorRing(), generator);

            for (var i = 0; i < value.getNumInteriorRing(); ++i) {
                serializeCoordinates(value.getInteriorRingN(i), generator);
            }
        }
        generator.writeEndArray();
    }

    private void serializeCoordinates(final LineString value, final JsonGenerator generator) {
        serializeCoordinates(value.getCoordinateSequence(), generator);
    }

    private void serializeCoordinates(
            final CoordinateSequence value, final JsonGenerator generator) {
        generator.writeStartArray();
        for (var i = 0; i < value.size(); ++i) {
            serializeCoordinate(value.getCoordinate(i), generator);
        }
        generator.writeEndArray();
    }

    private void serializeCoordinate(final Point value, final JsonGenerator generator) {
        serializeCoordinate(value.getCoordinate(), generator);
    }

    private void serializeCoordinate(final Coordinate value, final JsonGenerator generator) {
        generator.writeStartArray();
        generator.writeNumber(decimalFormat.format(value.getX()));
        generator.writeNumber(decimalFormat.format(value.getY()));
        if (Double.isFinite(value.getZ())) {
            generator.writeNumber(decimalFormat.format(value.getZ()));
        }
        generator.writeEndArray();
    }
}
