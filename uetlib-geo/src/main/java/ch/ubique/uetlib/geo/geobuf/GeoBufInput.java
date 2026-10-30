package ch.ubique.uetlib.geo.geobuf;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;

/** Typed inputs shared by the JSON adapter and the Java/JTS entry points. */
sealed interface GeoBufInput {
    Map<String, @Nullable Object> foreignMembers();

    record Collection(List<Feature> features, Map<String, @Nullable Object> foreignMembers)
            implements GeoBufInput {}

    record Feature(
            @Nullable Object id,
            @Nullable Shape geometry,
            @Nullable Map<String, @Nullable Object> properties,
            Map<String, @Nullable Object> foreignMembers)
            implements GeoBufInput {}

    /** Children retain foreign members on individual GeometryCollection entries. */
    record Shape(
            Geometry geometry, List<Shape> children, Map<String, @Nullable Object> foreignMembers)
            implements GeoBufInput {}
}
