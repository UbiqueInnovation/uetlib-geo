package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.*;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BoundingBoxUtil {

    private BoundingBoxUtil() {}

    public static Optional<BoundingBox<LV95Coordinate>> findBoundingBoxLv95(
            final Stream<LV95Coordinate> coordinateStream) {
        return findBoundingBox(
                coordinateStream,
                LV95Coordinate::getEast,
                LV95Coordinate::getNorth,
                LV95Coordinate::new);
    }

    public static Optional<BoundingBox<LV03Coordinate>> findBoundingBoxLv03(
            final Stream<LV03Coordinate> coordinateStream) {
        return findBoundingBox(
                coordinateStream,
                LV03Coordinate::getEast,
                LV03Coordinate::getNorth,
                LV03Coordinate::new);
    }

    public static Optional<BoundingBox<WGS84Coordinate>> findBoundingBoxWgs84(
            final Stream<WGS84Coordinate> coordinateStream) {
        return findBoundingBox(
                coordinateStream,
                WGS84Coordinate::getLatitude,
                WGS84Coordinate::getLongitude,
                WGS84Coordinate::new);
    }

    public static Optional<BoundingBox<WebMercatorCoordinate>> findBoundingBoxWebMercator(
            final Stream<WebMercatorCoordinate> coordinateStream) {
        return findBoundingBox(
                coordinateStream,
                WebMercatorCoordinate::getX,
                WebMercatorCoordinate::getY,
                WebMercatorCoordinate::new);
    }

    private static <C extends Coordinate> Optional<BoundingBox<C>> findBoundingBox(
            final Stream<C> coordinateStream,
            final Function<C, Double> firstOordinate,
            final Function<C, Double> secondOordinate,
            final BiFunction<Double, Double, C> coordinateConstructor) {
        final var oordinates =
                coordinateStream.collect(
                        Collectors.teeing(
                                Collectors.mapping(firstOordinate, Collectors.toList()),
                                Collectors.mapping(secondOordinate, Collectors.toList()),
                                Pair::of));

        final var firstOordinates = oordinates.getLeft();
        final var secondOordinates = oordinates.getRight();

        if (firstOordinates.isEmpty()) {
            return Optional.empty();
        }

        final var minFirstOordinate =
                firstOordinates.stream().mapToDouble(d -> d).min().orElseThrow();
        final var maxFirstOordinate =
                firstOordinates.stream().mapToDouble(d -> d).max().orElseThrow();
        final var minSecondOordinate =
                secondOordinates.stream().mapToDouble(d -> d).min().orElseThrow();
        final var maxSecondOordinate =
                secondOordinates.stream().mapToDouble(d -> d).max().orElseThrow();

        final var min = coordinateConstructor.apply(minFirstOordinate, minSecondOordinate);
        final var max = coordinateConstructor.apply(maxFirstOordinate, maxSecondOordinate);
        return Optional.of(new BoundingBox<>(min, max));
    }
}
