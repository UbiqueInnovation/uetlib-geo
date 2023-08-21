package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.core.utils.MinMaxCollector;
import ch.ubique.backend.geo.coordinate.models.*;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BoundingBoxUtil {

    private BoundingBoxUtil() {}

    public static <C extends Coordinate> Optional<BoundingBox<C>> findBoundingBox(
            final Stream<C> coordinateStream) {

        final var minMaxPerDimensionCollector =
                Collectors.teeing(
                        Collectors.mapping((C c) -> c.toJts().x, MinMaxCollector.collectOptional()),
                        Collectors.mapping((C c) -> c.toJts().y, MinMaxCollector.collectOptional()),
                        Pair::of);

        final var anyCoordinateCollector = Collectors.reducing(null, (C ignored, C c) -> c);

        final var anyCoordinateAndMinMax =
                coordinateStream.collect(
                        Collectors.teeing(
                                anyCoordinateCollector, minMaxPerDimensionCollector, Pair::of));

        final var anyCoordinate = anyCoordinateAndMinMax.getKey();
        final var minMax = anyCoordinateAndMinMax.getValue();

        if (anyCoordinate == null) {
            return Optional.empty();
        }

        final var jtsConverter = JtsConverter.forInstance(anyCoordinate);

        final var minMaxX = minMax.getLeft().orElseThrow();
        final var minMaxY = minMax.getRight().orElseThrow();

        final var min =
                jtsConverter.toBackendLib(
                        new org.locationtech.jts.geom.Coordinate(minMaxX.min(), minMaxY.min()));
        final var max =
                jtsConverter.toBackendLib(
                        new org.locationtech.jts.geom.Coordinate(minMaxX.max(), minMaxY.max()));
        return Optional.of(new BoundingBox<>(min, max));
    }
}
