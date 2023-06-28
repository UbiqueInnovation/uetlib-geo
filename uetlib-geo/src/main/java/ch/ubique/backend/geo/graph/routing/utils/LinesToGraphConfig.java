package ch.ubique.backend.geo.graph.routing.utils;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public record LinesToGraphConfig<T>(
        double snapTolerance,
        double verticalSplitThreshold,
        WeightingFunction<T> weightingFunction,
        ToDoubleFunction<T> snappingPriority,
        ToDoubleFunction<T> edgePriority,
        Predicate<T> isHighway) {

    public static <S> LinesToGraphConfig<S> defaults() {
        return LinesToGraphConfig.<S>builder().build();
    }

    public static <S> LinesToGraphConfigBuilder<S> builder() {
        return new LinesToGraphConfigBuilder<>();
    }

    public static class LinesToGraphConfigBuilder<T> {

        private double snapTolerance;
        private double verticalSplitThreshold;
        private WeightingFunction<T> weightingFunction;
        private ToDoubleFunction<T> snappingPriority;
        private ToDoubleFunction<T> edgePriority;
        private Predicate<T> isHighway;

        private LinesToGraphConfigBuilder() {
            snapTolerance = 1;
            verticalSplitThreshold = 50;
            weightingFunction = (from, to, context) -> (float) from.distance(to);
            snappingPriority = t -> 0;
            edgePriority = t -> 0;
            isHighway = t -> false;
        }

        public LinesToGraphConfigBuilder<T> snapTolerance(final double snapTolerance) {
            this.snapTolerance = snapTolerance;
            return this;
        }

        public LinesToGraphConfigBuilder<T> verticalSplitThreshold(
                final double verticalSplitThreshold) {
            this.verticalSplitThreshold = verticalSplitThreshold;
            return this;
        }

        public LinesToGraphConfigBuilder<T> weightingFunction(
                final WeightingFunction<T> weightingFunction) {
            this.weightingFunction = Objects.requireNonNull(weightingFunction);
            return this;
        }

        /**
         * Set a predicate that determines whether vertices of the given line should be considered
         * highway vertices.
         */
        public LinesToGraphConfigBuilder<T> isHighway(final Predicate<T> isHighway) {
            this.isHighway = Objects.requireNonNull(isHighway);
            return this;
        }

        /**
         * Shortcut for {@link LinesToGraphConfigBuilder#snappingPriority}.Lines that match the
         * predicate have priority over lines, that do not match it.
         */
        public LinesToGraphConfigBuilder<T> hasSnappingPriority(final Predicate<T> hasPriority) {
            Objects.requireNonNull(hasPriority);
            return snappingPriority(x -> hasPriority.test(x) ? 1 : 0);
        }

        /**
         * A higher priority is better. I.e. vertices from lines of higher priority are preferred
         * for snapping
         */
        public LinesToGraphConfigBuilder<T> snappingPriority(
                final ToDoubleFunction<T> priorityExtractor) {
            this.snappingPriority = Objects.requireNonNull(priorityExtractor);
            return this;
        }

        /**
         * Shortcut for {@link LinesToGraphConfigBuilder#edgePriority}.Lines that match the
         * predicate have priority over lines, that do not match it.
         */
        public LinesToGraphConfigBuilder<T> hasEdgePriority(final Predicate<T> hasPriority) {
            Objects.requireNonNull(hasPriority);
            return edgePriority(x -> hasPriority.test(x) ? 1 : 0);
        }

        /**
         * A higher priority is better. I.e. vertices from lines of higher priority are preferred
         * for when inserting edges between the same vertices.
         */
        public LinesToGraphConfigBuilder<T> edgePriority(
                final ToDoubleFunction<T> priorityExtractor) {
            this.edgePriority = Objects.requireNonNull(priorityExtractor);
            return this;
        }

        public LinesToGraphConfig<T> build() {
            return new LinesToGraphConfig<>(
                    snapTolerance,
                    verticalSplitThreshold,
                    weightingFunction,
                    snappingPriority,
                    edgePriority,
                    isHighway);
        }
    }
}
