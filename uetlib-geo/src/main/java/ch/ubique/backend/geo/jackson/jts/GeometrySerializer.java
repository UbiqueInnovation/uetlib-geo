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
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * {@link ValueSerializer} for {@link Geometry}.
 *
 * @author Christian Autermann
 */
public class GeometrySerializer extends ValueSerializer<Geometry> {
    static final int DEFAULT_DECIMAL_PLACES = 8;
    private final NumberFormat decimalFormat;
    private final IncludeBoundingBox includeBoundingBox;

    /** Creates a new {@link GeometrySerializer}. */
    public GeometrySerializer() {
        this(null, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * Creates a new {@link GeometrySerializer}.
     *
     * @param includeBoundingBox when to include a bounding box for a {@link Geometry}.
     */
    public GeometrySerializer(@Nullable final IncludeBoundingBox includeBoundingBox) {
        this(includeBoundingBox, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * Creates a new {@link GeometrySerializer}.
     *
     * @param includeBoundingBox when to include a bounding box for a {@link Geometry}.
     * @param decimalPlaces The number of decimal places for encoded coordinates.
     */
    public GeometrySerializer(
            @Nullable final IncludeBoundingBox includeBoundingBox, final int decimalPlaces) {
        this.includeBoundingBox =
                Optional.ofNullable(includeBoundingBox).orElseGet(IncludeBoundingBox::never);
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("decimalPlaces < 0");
        }
        this.decimalFormat = createNumberFormat(decimalPlaces);
    }

    private NumberFormat createNumberFormat(final int decimalPlaces) {
        final var format = DecimalFormat.getInstance(Locale.ROOT);
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
            final SerializationContext provider)
            {
        switch (geometry) {
            case null -> generator.writeNull();
            case final Polygon polygon -> serialize(polygon, generator, provider);
            case final Point point -> serialize(point, generator, provider);
            case final MultiPoint multiPoint -> serialize(multiPoint, generator, provider);
            case final MultiPolygon multiPolygon -> serialize(multiPolygon, generator, provider);
            case final LineString lineString -> serialize(lineString, generator, provider);
            case final MultiLineString multiLineString ->
                    serialize(multiLineString, generator, provider);
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

        serializeTypeAndBoundingBox(GeometryType.GEOMETRY_COLLECTION, value, generator);

        generator.writeArrayPropertyStart(Field.GEOMETRIES);

        for (var i = 0; i != value.getNumGeometries(); ++i) {
            serialize(value.getGeometryN(i), generator, provider);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(
            final MultiPoint value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.MULTI_POINT, value, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES);

        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinate((Point) value.getGeometryN(i), generator, provider);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(
            final MultiLineString value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.MULTI_LINE_STRING, value, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES);
        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinates((LineString) value.getGeometryN(i), generator, provider);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(
            final MultiPolygon value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.MULTI_POLYGON, value, generator);
        generator.writeArrayPropertyStart(Field.COORDINATES);
        for (var i = 0; i < value.getNumGeometries(); ++i) {
            serializeCoordinates((Polygon) value.getGeometryN(i), generator, provider);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void serialize(
            final Polygon value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.POLYGON, value, generator);
        generator.writeName(Field.COORDINATES);
        serializeCoordinates(value, generator, provider);
        generator.writeEndObject();
    }

    private void serialize(
            final LineString value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.LINE_STRING, value, generator);
        generator.writeName(Field.COORDINATES);
        serializeCoordinates(value, generator, provider);
        generator.writeEndObject();
    }

    private void serialize(
            final Point value, final JsonGenerator generator, final SerializationContext provider) {
        generator.writeStartObject();
        serializeTypeAndBoundingBox(GeometryType.POINT, value, generator);
        generator.writeName(Field.COORDINATES);
        serializeCoordinate(value, generator, provider);
        generator.writeEndObject();
    }

    private void serializeTypeAndBoundingBox(
            final GeometryType type, final Geometry geometry, final JsonGenerator generator) {

        generator.writeStringProperty(Field.TYPE, type.toString());

        if (includeBoundingBox.shouldIncludeBoundingBoxFor(type) && !geometry.isEmpty()) {
            final var envelope = geometry.getEnvelopeInternal();
            generator.writeArrayPropertyStart(Field.BOUNDING_BOX);
            generator.writeNumber(envelope.getMinX());
            generator.writeNumber(envelope.getMinY());
            generator.writeNumber(envelope.getMaxX());
            generator.writeNumber(envelope.getMaxY());
            generator.writeEndArray();
        }
    }

    private void serializeCoordinates(
            final Polygon value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartArray();
        if (!value.isEmpty()) {
            serializeCoordinates(value.getExteriorRing(), generator, provider);

            for (var i = 0; i < value.getNumInteriorRing(); ++i) {
                serializeCoordinates(value.getInteriorRingN(i), generator, provider);
            }
        }
        generator.writeEndArray();
    }

    private void serializeCoordinates(
            final LineString value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        serializeCoordinates(value.getCoordinateSequence(), generator, provider);
    }

    private void serializeCoordinates(
            final CoordinateSequence value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartArray();
        for (var i = 0; i < value.size(); ++i) {
            serializeCoordinate(value.getCoordinate(i), generator, provider);
        }
        generator.writeEndArray();
    }

    private void serializeCoordinate(
            final Point value, final JsonGenerator generator, final SerializationContext provider) {
        serializeCoordinate(value.getCoordinate(), generator, provider);
    }

    private void serializeCoordinate(
            final Coordinate value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartArray();
        generator.writeNumber(decimalFormat.format(value.getX()));
        generator.writeNumber(decimalFormat.format(value.getY()));
        if (!Double.isNaN(value.getZ()) && Double.isFinite(value.getZ())) {
            generator.writeNumber(decimalFormat.format(value.getZ()));
        }
        generator.writeEndArray();
    }
}
