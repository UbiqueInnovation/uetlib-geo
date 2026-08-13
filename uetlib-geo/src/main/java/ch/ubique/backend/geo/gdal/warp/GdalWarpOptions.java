package ch.ubique.backend.geo.gdal.warp;

import static java.util.Objects.requireNonNull;

import ch.ubique.backend.geo.common.models.WidthAndHeight;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.gdal.common.GdalDataSetFormat;

import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public final class GdalWarpOptions {
    private @Nullable Collection<String> sourceNoDataValues;
    private @Nullable Collection<String> targetNoDataValues;
    private @Nullable WidthAndHeight<Integer> targetSize;
    private @Nullable GdalResamplingMethod resamplingMethod;
    private @Nullable String sourceSpatialReferenceSystem;
    private @Nullable String targetSpatialReferenceSystem;
    private @Nullable GdalDataSetFormat outputFormat;
    private @Nullable Path sourcePath;
    private @Nullable Path targetPath;
    private @Nullable List<String> additionalArguments;

    private GdalWarpOptions() {}

    public static GdalWarpOptions defaults() {
        return new GdalWarpOptions();
    }

    public GdalWarpOptions additionalArguments(final @Nullable List<String> additionalArguments) {
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

        if (sourceSpatialReferenceSystem != null) {
            args.add("-s_srs");
            args.add(sourceSpatialReferenceSystem);
        }

        if (targetSpatialReferenceSystem != null) {
            args.add("-t_srs");
            args.add(targetSpatialReferenceSystem);
        }

        if (outputFormat != null) {
            args.add("-of");
            args.add(outputFormat.shortName());
        }

        if (CollectionUtils.isNotEmpty(additionalArguments)) {
            args.addAll(additionalArguments);
        }

        args.add(requireNonNull(sourcePath).toString());
        args.add(requireNonNull(targetPath).toString());

        return Collections.unmodifiableList(args);
    }

    public GdalWarpOptions sourceNoDataValues(final String... sourceNoDataValues) {
        return sourceNoDataValues(List.of(sourceNoDataValues));
    }

    public GdalWarpOptions sourceNoDataValues(
            final @Nullable Collection<String> sourceNoDataValues) {
        this.sourceNoDataValues = sourceNoDataValues;
        return this;
    }

    public GdalWarpOptions targetNoDataValues(final String... targetNoDataValues) {
        return targetNoDataValues(List.of(targetNoDataValues));
    }

    public GdalWarpOptions targetNoDataValues(
            final @Nullable Collection<String> targetNoDataValues) {
        this.targetNoDataValues = targetNoDataValues;
        return this;
    }

    public GdalWarpOptions targetSize(final @Nullable WidthAndHeight<Integer> targetSize) {
        this.targetSize = targetSize;
        return this;
    }

    public GdalWarpOptions resamplingMethod(final @Nullable GdalResamplingMethod resamplingMethod) {
        this.resamplingMethod = resamplingMethod;
        return this;
    }

    /**
     * Sets the source SRS by EPSG code.
     *
     * @see #sourceSRSString(String) to set the source SRS by WKT string, PROJ.4 declaration, or
     *     name of a .prj file containing a WKT CRS definition. Calling one after the other will
     *     make the last call take precedence.
     */
    public GdalWarpOptions sourceCoordinateSystem(
            final @Nullable CoordinateSystem<?> sourceCoordinateSystem) {
        if (sourceCoordinateSystem == null) {
            this.sourceSpatialReferenceSystem = null;
            return this;
        }
        this.sourceSpatialReferenceSystem = "EPSG:" + sourceCoordinateSystem.srid();
        return this;
    }

    /**
     * @param sourceSRS Can be a WKT string, PROJ.4 declaration, or name of a .prj file containing a
     *     WKT CRS definition.
     * @see <a href="https://gdal.org/programs/gdalwarp.html#cmdoption-gdalwarp-s_srs">Gdalwarp
     *     docs</a> for more information.
     * @see #sourceCoordinateSystem(CoordinateSystem) to set the source SRS by EPSG code. Calling
     *     one after the other will make the last call take precedence.
     */
    public GdalWarpOptions sourceSRSString(final @Nullable String sourceSRS) {
        this.sourceSpatialReferenceSystem = sourceSRS;
        return this;
    }

    /**
     * Sets the target SRS by EPSG code.
     *
     * @see #targetSRSString(String) to set the target SRS by WKT string, PROJ.4 declaration, or
     *     name of a .prj file containing a WKT CRS definition. Calling one after the other will
     *     make the last call take precedence.
     */
    public GdalWarpOptions targetCoordinateSystem(
            final @Nullable CoordinateSystem<?> targetCoordinateSystem) {
        if (targetCoordinateSystem == null) {
            this.targetSpatialReferenceSystem = null;
            return this;
        }
        this.targetSpatialReferenceSystem = "EPSG:" + targetCoordinateSystem.srid();
        return this;
    }

    /**
     * @param targetSRS Can be a WKT string, PROJ.4 declaration, or name of a .prj file containing a
     *     WKT CRS definition.
     * @see <a href="https://gdal.org/programs/gdalwarp.html#cmdoption-gdalwarp-t_srs">Gdalwarp
     *     docs</a> for more information.
     * @see #targetCoordinateSystem(CoordinateSystem) to set the target SRS by EPSG code. Calling
     *     one after the other will make the last call take precedence.
     */
    public GdalWarpOptions targetSRSString(final @Nullable String targetSRS) {
        this.targetSpatialReferenceSystem = targetSRS;
        return this;
    }

    public GdalWarpOptions outputFormat(final @Nullable GdalDataSetFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public GdalWarpOptions sourceFile(final @Nullable Path sourceFile) {
        this.sourcePath = sourceFile;
        return this;
    }

    public GdalWarpOptions targetFile(final @Nullable Path targetFile) {
        this.targetPath = targetFile;
        return this;
    }
}
