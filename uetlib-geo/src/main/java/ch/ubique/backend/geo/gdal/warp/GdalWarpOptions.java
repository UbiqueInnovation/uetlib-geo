package ch.ubique.backend.geo.gdal.warp;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.gdal.common.GdalDataSetFormat;

import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public record GdalWarpOptions(
        Collection<String> noDataValues,
        Collection<String> destinationNoDataValues,
        Integer width,
        Integer height,
        GdalResamplingMethod resamplingMethod,
        CoordinateSystem<?> targetCoordinateSystem,
        GdalDataSetFormat outputFormat,
        Path sourceFile,
        Path targetFile,
        Duration timeout) {
    public static GdalWarpOptionsBuilder builder() {
        return new GdalWarpOptionsBuilder();
    }

    public List<String> buildCommand() {
        final var args = new ArrayList<String>();
        args.add("gdalwarp");

        if (CollectionUtils.isNotEmpty(noDataValues)) {
            args.add("-srcnodata");
            args.addAll(noDataValues);
        }

        if (CollectionUtils.isNotEmpty(destinationNoDataValues)) {
            args.add("-dstnodata");
            args.addAll(destinationNoDataValues);
        }

        if (width != null && height != null) {
            args.add("-ts");
            args.add(String.valueOf(width));
            args.add(String.valueOf(height));
        }

        if (resamplingMethod != null) {
            args.add("-r");
            args.add(resamplingMethod.value());
        }

        if (targetCoordinateSystem != null) {
            args.add("-t_srs");
            args.add("EPSG:" + targetCoordinateSystem.srid());
        }

        if (outputFormat != null) {
            args.add("-of");
            args.add(outputFormat.shortName());
        }

        args.add(Objects.requireNonNull(sourceFile).toString());
        args.add(Objects.requireNonNull(targetFile).toString());

        return Collections.unmodifiableList(args);
    }

    public static class GdalWarpOptionsBuilder {
        private Collection<String> srcNoDataValues;
        private Collection<String> destinationNoDataValues;
        private Integer width;
        private Integer height;
        private GdalResamplingMethod resamplingMethod;
        private CoordinateSystem<?> targetSpatialReference;
        private GdalDataSetFormat outputFormat;
        private Path sourcePath;
        private Path targetPath;
        private Duration timeout = Duration.ofMinutes(1);

        private GdalWarpOptionsBuilder() {}

        public GdalWarpOptionsBuilder srcNoDataValues(final Collection<String> srcNoDataValues) {
            this.srcNoDataValues = srcNoDataValues;
            return this;
        }

        public GdalWarpOptionsBuilder destinationNoDataValues(
                final Collection<String> destinationNoDataValues) {
            this.destinationNoDataValues = destinationNoDataValues;
            return this;
        }

        public GdalWarpOptionsBuilder width(final int width) {
            this.width = width;
            return this;
        }

        public GdalWarpOptionsBuilder height(final int height) {
            this.height = height;
            return this;
        }

        public GdalWarpOptionsBuilder resamplingMethod(
                final GdalResamplingMethod resamplingMethod) {
            this.resamplingMethod = resamplingMethod;
            return this;
        }

        public GdalWarpOptionsBuilder targetSpatialReference(
                final CoordinateSystem<?> targetSpatialReference) {
            this.targetSpatialReference = targetSpatialReference;
            return this;
        }

        public GdalWarpOptionsBuilder outputFormat(final GdalDataSetFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public GdalWarpOptionsBuilder sourceFile(final Path sourceFile) {
            this.sourcePath = sourceFile;
            return this;
        }

        public GdalWarpOptionsBuilder targetFile(final Path targetFile) {
            this.targetPath = targetFile;
            return this;
        }

        public GdalWarpOptionsBuilder timeout(final Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public GdalWarpOptions build() {
            return new GdalWarpOptions(
                    srcNoDataValues,
                    destinationNoDataValues,
                    width,
                    height,
                    resamplingMethod,
                    targetSpatialReference,
                    outputFormat,
                    Objects.requireNonNull(sourcePath, "Source path is required"),
                    Objects.requireNonNull(targetPath, "Target path is required"),
                    timeout);
        }
    }
}
