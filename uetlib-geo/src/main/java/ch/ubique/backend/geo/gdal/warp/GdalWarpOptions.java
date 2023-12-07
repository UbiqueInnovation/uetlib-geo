package ch.ubique.backend.geo.gdal.warp;

import ch.ubique.backend.geo.gdal.common.GdalDataSetFormat;
import ch.ubique.backend.geo.gdal.common.Proj4Declaration;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

public record GdalWarpOptions(
        Collection<String> noDataValues,
        Collection<String> destinationNoDataValues,
        int width,
        int height,
        GdalResamplingMethod resamplingMethod,
        Proj4Declaration targetSpatialReference,
        GdalDataSetFormat outputFormat,
        Path sourceFile,
        Path targetFile,
        Duration timeout) {
    public static GdalWarpOptionsBuilder builder() {
        return new GdalWarpOptionsBuilder();
    }

    public String[] buildCommand() {
        return new String[] {
            "gdalwarp",
            "-srcnodata",
            String.join(" ", noDataValues),
            "-dstnodata",
            String.join(" ", destinationNoDataValues),
            "-ts",
            String.valueOf(width),
            String.valueOf(height),
            "-r",
            resamplingMethod.value(),
            "-t_srs",
            targetSpatialReference.toProj4String(),
            "-of",
            outputFormat.shortName(),
            sourceFile.toString(),
            targetFile.toString()
        };
    }

    public static class GdalWarpOptionsBuilder {
        private Collection<String> srcNoDataValues;
        private Collection<String> destinationNoDataValues;
        private int width;
        private int height;
        private GdalResamplingMethod resamplingMethod;
        private Proj4Declaration targetSpatialReference;
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
                final Proj4Declaration targetSpatialReference) {
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
