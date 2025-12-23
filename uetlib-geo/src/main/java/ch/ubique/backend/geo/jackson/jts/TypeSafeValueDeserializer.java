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

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.type.TypeFactory;

import java.util.Objects;

/**
 * Type safe variant of a JsonDeserializer that checks the parsed value is of the right type.
 *
 * @param <T> The type to be accepted.
 */
public class TypeSafeValueDeserializer<T> extends ValueDeserializer<T> {
    private final ValueDeserializer<? super T> delegate;
    private final JavaType type;

    /**
     * Creates a new {@link TypeSafeValueDeserializer}.
     *
     * @param type The type to be accepted.
     * @param delegate The {@link ValueDeserializer} to delegate to.
     */
    public TypeSafeValueDeserializer(
            final Class<? extends T> type, final ValueDeserializer<? super T> delegate) {
        this(TypeFactory.createDefaultInstance().constructType(type), delegate);
    }

    /**
     * Creates a new {@link TypeSafeValueDeserializer}.
     *
     * @param type The type to be accepted.
     * @param delegate The {@link ValueDeserializer} to delegate to.
     */
    public TypeSafeValueDeserializer(
            final JavaType type, final ValueDeserializer<? super T> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(final JsonParser p, final DeserializationContext context) {
        final var obj = delegate.deserialize(p, context);
        if (obj == null) {
            return null;
        }

        if (type.isTypeOrSuperTypeOf(obj.getClass())) {
            return (T) obj;
        }

        throw DatabindException.from(
                context, String.format("Invalid type for %s: %s", type, obj.getClass()));
    }
}
