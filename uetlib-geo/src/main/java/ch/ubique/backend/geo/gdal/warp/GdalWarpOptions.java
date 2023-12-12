package ch.ubique.backend.geo.gdal.warp;

import ch.ubique.backend.geo.common.models.WidthAndHeight;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.gdal.common.GdalDataSetFormat;

import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.*;

public final class GdalWarpOptions {
    private Collection<String> sourceNoDataValues;
    private Collection<String> targetNoDataValues;
    private WidthAndHeight<Integer> targetSize;
    private GdalResamplingMethod resamplingMethod;
    private CoordinateSystem<?> targetCoordinateSystem;
    private GdalDataSetFormat outputFormat;
    private Path sourcePath;
    private Path targetPath;
    private List<String> additionalArguments;

    private GdalWarpOptions() {}

    public static GdalWarpOptions defaults() {
        return new GdalWarpOptions();
    }

    public GdalWarpOptions additionalArguments(final List<String> additionalArguments) {
        this.additionalArguments = additionalArguments;
        return this;
    }

    public List<String> toCommand() {
        final var args = new ArrayList<String>();
        args.add("gdalwarp");

        if (CollectionUtils.isNotEmpty(sourceNoDataValues)) {
            args.add("-srcnodata");
            args.addAll(sourceNoDataValues);
        }

        if (CollectionUtils.isNotEmpty(targetNoDataValues)) {
            args.add("-dstnodata");
            args.addAll(targetNoDataValues);
        }

        if (targetSize != null) {
            args.add("-ts");
            args.add(String.valueOf(targetSize.width()));
            args.add(String.valueOf(targetSize.height()));
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

        if (additionalArguments != null && !additionalArguments.isEmpty()) {
            args.addAll(additionalArguments);
        }

        args.add(Objects.requireNonNull(sourcePath).toString());
        args.add(Objects.requireNonNull(targetPath).toString());

        return Collections.unmodifiableList(args);
    }

    public GdalWarpOptions sourceNoDataValues(final Collection<String> sourceNoDataValues) {
        this.sourceNoDataValues = sourceNoDataValues;
        return this;
    }

    public GdalWarpOptions targetNoDataValues(final Collection<String> targetNoDataValues) {
        this.targetNoDataValues = targetNoDataValues;
        return this;
    }

    public GdalWarpOptions targetSize(final WidthAndHeight<Integer> targetSize) {
        this.targetSize = targetSize;
        return this;
    }

    public GdalWarpOptions resamplingMethod(final GdalResamplingMethod resamplingMethod) {
        this.resamplingMethod = resamplingMethod;
        return this;
    }

    public GdalWarpOptions targetCoordinateSystem(
            final CoordinateSystem<?> targetCoordinateSystem) {
        this.targetCoordinateSystem = targetCoordinateSystem;
        return this;
    }

    public GdalWarpOptions outputFormat(final GdalDataSetFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public GdalWarpOptions sourceFile(final Path sourceFile) {
        this.sourcePath = sourceFile;
        return this;
    }

    public GdalWarpOptions targetFile(final Path targetFile) {
        this.targetPath = targetFile;
        return this;
    }
}
