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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Optional;

public enum GeometryType {
    /** Geometries of type {@link Point}. */
    POINT(Type.POINT),
    /** Geometries of type {@link LineString}. */
    LINE_STRING(Type.LINE_STRING),
    /** Geometries of type {@link Polygon}. */
    POLYGON(Type.POLYGON),
    /** Geometries of type {@link MultiPoint}. */
    MULTI_POINT(Type.MULTI_POINT),
    /** Geometries of type {@link MultiLineString}. */
    MULTI_LINE_STRING(Type.MULTI_LINE_STRING),
    /** Geometries of type {@link MultiPolygon}. */
    MULTI_POLYGON(Type.MULTI_POLYGON),
    /** Geometries of type {@link GeometryCollection}. */
    GEOMETRY_COLLECTION(Type.GEOMETRY_COLLECTION);

    private final String type;

    /**
     * Create a new {@link GeometryType}.
     *
     * @param type The string type.
     */
    GeometryType(final String type) {
        this.type = type;
    }

    /**
     * The bit mask value of this {@link GeometryType}.
     *
     * @return The bit mask.
     */
    int mask() {
        return 1 << ordinal();
    }

    @Override
    @JsonValue
    public String toString() {
        return type;
    }

    /**
     * Get the geometry type from the supplied GeoJSON type value.
     *
     * @param value The GeoJSON type.
     * @return The {@link GeometryType}
     */
    @JsonCreator
    public static Optional<GeometryType> fromString(final String value) {
        for (final var type : GeometryType.values()) {
            if (type.toString().equals(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the geometry type from the supplied {@link Geometry}.
     *
     * @param geometry The {@link Geometry}.
     * @return The {@link GeometryType}.
     */
    public static Optional<GeometryType> forGeometry(final Geometry geometry) {
        return switch (geometry) {
            case final Polygon ignored -> Optional.of(POLYGON);
            case final Point ignored -> Optional.of(POINT);
            case final MultiPoint ignored -> Optional.of(MULTI_POINT);
            case final MultiPolygon ignored -> Optional.of(MULTI_POLYGON);
            case final LineString ignored -> Optional.of(LINE_STRING);
            case final MultiLineString ignored -> Optional.of(MULTI_LINE_STRING);
            case final GeometryCollection ignored -> Optional.of(GEOMETRY_COLLECTION);
            case null, default -> Optional.empty();
        };
    }
}
