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

import org.jspecify.annotations.Nullable;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * {@link JacksonModule} to add serialization for JTS geometries.
 *
 * @author Christian Autermann
 */
public class JtsModule extends SimpleModule {

    private final GeometryFactory geometryFactory;
    private final int decimalPlaces;

    /** Creates a new {@link JtsModule}. */
    public JtsModule() {
        this(null, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
    }

    /**
     * Creates a new {@link JtsModule}.
     *
     * @param geometryFactory The {@link GeometryFactory} to use to construct geometries.
     */
    public JtsModule(@Nullable final GeometryFactory geometryFactory) {
        this(geometryFactory, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
    }

    /**
     * Creates a new {@link JtsModule}.
     *
     * @param geometryFactory The {@link GeometryFactory} to use to construct geometries.
     * @param decimalPlaces The number of decimal places for encoded coordinates.
     */
    public JtsModule(@Nullable final GeometryFactory geometryFactory, final int decimalPlaces) {
        this.geometryFactory = geometryFactory;
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("decimalPlaces < 0");
        }
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public void setupModule(final SetupContext context) {
        final var deserializer = getDeserializer();
        addSerializer(Geometry.class, getSerializer());
        addDeserializer(Geometry.class, deserializer);
        addDeserializer(Point.class, new TypeSafeValueDeserializer<>(Point.class, deserializer));
        addDeserializer(
                LineString.class, new TypeSafeValueDeserializer<>(LineString.class, deserializer));
        addDeserializer(
                Polygon.class, new TypeSafeValueDeserializer<>(Polygon.class, deserializer));
        addDeserializer(
                MultiPoint.class, new TypeSafeValueDeserializer<>(MultiPoint.class, deserializer));
        addDeserializer(
                MultiLineString.class,
                new TypeSafeValueDeserializer<>(MultiLineString.class, deserializer));
        addDeserializer(
                MultiPolygon.class,
                new TypeSafeValueDeserializer<>(MultiPolygon.class, deserializer));
        addDeserializer(
                GeometryCollection.class,
                new TypeSafeValueDeserializer<>(GeometryCollection.class, deserializer));
        super.setupModule(context);
    }

    private ValueSerializer<Geometry> getSerializer() {
        return new GeometrySerializer(decimalPlaces);
    }

    private ValueDeserializer<Geometry> getDeserializer() {
        return new GeometryDeserializer(geometryFactory);
    }
}
