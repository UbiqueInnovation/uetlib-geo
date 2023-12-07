package ch.ubique.backend.geo.gdal;

import java.nio.file.Path;
import java.util.Collection;

public record GdalWarpOptions(
        Collection<String> noDataValues,
        Collection<String> destinationNoDataValues,
        int width,
        int height,
        FileConverterResamplingMethod resamplingMethod,
        Proj4Declaration targetSpatialReference,
        FileConverterOutputFormat outputFormat,
        Path sourceFile,
        Path targetFile) {
    public static FileConverterOptionsBuilder builder() {
        return new FileConverterOptionsBuilder();
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
            resamplingMethod.resamplingMethod,
            "-t_srs",
            targetSpatialReference.toProj4String(),
            "-of",
            outputFormat.shortName,
            sourceFile.toString(),
            targetFile.toString()
        };
    }

    public enum FileConverterResamplingMethod {
        NEAREST("nearest"),
        BILINEAR("bilinear"),
        CUBIC("cubic"),
        CUBICSPLINE("cubicspline"),
        LANCZOS("lanczos"),
        AVERAGE("average"),
        ROOT_MEAN_SQUARE("rms"),
        MODE("mode"),
        MAX("max"),
        MIN("min"),
        MED("med"),
        Q1("q1"),
        Q3("q3"),
        SUM("sum");

        private final String resamplingMethod;

        FileConverterResamplingMethod(final String resamplingMethod) {
            this.resamplingMethod = resamplingMethod;
        }
    }

    /** Full list of output formats: {@code gdalinfo --formats} */
    public enum FileConverterOutputFormat {
        VRT("VRT"),
        DERIVED("DERIVED"),
        GEO_TIFF("GTiff"),
        CLOUD_OPTIMIZED_GEO_TIFF("COG"),
        NATIONAL_IMAGERY_TRANSMISSION_FORMAT("NITF"),
        RASTER_PRODUCT_FORMAT_TOC_FORMAT("RPFTOC"),
        ECRG_TOC_FORMAT("ECRGTOC"),
        ERDAS_IMAGINE_IMAGES("HFA"),
        CEOS_SAR_IMAGE("SAR_CEOS"),
        CEOS_IMAGE("CEOS"),
        JAXA_PALSAR_PRODUCT_READER("JAXAPALSAR"),
        GROUND_BASED_SAR_APPLICATIONS_TESTBED_FILE_FORMAT("GFF"),
        ELAS("ELAS"),
        ESRI_COMPACT_CACHE("ESRIC"),
        ARC_INFO_BINARY_GRID("AIG"),
        ARC_INFO_ASCII_GRID("AAIGrid"),
        GRASS_ASCII_GRID("GRASSASCIIGrid"),
        INTERNATIONAL_SERVICE_FOR_THE_GEOID("ISG"),
        SDTS("SDTS"),
        DTED_ELEVATION_RASTER("DTED"),
        PORTABLE_NETWORK_GRAPHICS("PNG"),
        JPEG_JFIF("JPEG"),
        IN_MEMORY_RASTER("MEM"),
        JAXA_PALSAR_PRODUCT_READER_2("JDEM"),
        GRAPHICS_INTERCHANGE_FORMAT("GIF"),
        GRAPHICS_INTERCHANGE_FORMAT_2("BIGGIF"),
        ENVISAT_IMAGE_FORMAT("ESAT"),
        FLEXIBLE_IMAGE_TRANSPORT_SYSTEM("FITS"),
        MAPTECH_BSB_NAUTICAL_CHARTS("BSB"),
        X11_PIXMAP_FORMAT("XPM"),

        MS_WINDOWS_DEVICE_INDEPENDENT_BITMAP("BMP"),
        SPOT_DIMAP("DIMAP"),
        AIRSAR_POLARIMETRIC_IMAGE("AirSAR"),
        RADARSAT_2_XML_PRODUCT("RS2"),
        SENTINEL_1_SAR_SAFE_PRODUCT("SAFE"),
        PCIDSK_DATABASE_FILE("PCIDSK"),
        PC_RASTER("PCRaster"),
        ILWIS("ILWIS"),
        SGI_IMAGE_FILE_FORMAT_1_0("SGI"),

        SRTMHGT_FILE_FORMAT("SRTMHGT"),
        LEVELLER("Leveller"),
        TERRAGEN("Terragen"),
        NETWORK_COMMON_DATA_FORMAT("netCDF"),
        HIERARCHICAL_DATA_FORMAT_RELEASE_4("HDF4"),
        HDF4DATASET("HDF4Image"),
        GRIB("GRIB"),
        SAGA_GIS_BINARY_GRID("SAGA"),
        ASCII_GRID("XYZ"),
        HF2("HF2"),
        OZI_EXPLORER_IMAGE_FILE("OZI"),
        USGS_LULC_COMPOSITE_THEME_GRID("CTG"),
        ZMAP_PLUS_GRID("ZMap"),
        NOAA_NGS_GEOID_HEIGHT_GRIDS("NGSGEOID"),
        IRIS_DATA("IRIS"),
        RACURS_PHOTOMOD_PRF("PRF"),
        EARTH_ENGINE_DATA_API_IMAGE("EEDAI"),
        AIRBUS_DS_INTELLIGENCE_DATA_AS_A_SERVICE_DRIVER("DAAS"),
        SCALED_INTEGER_GRIDDED_DEM("SIGDEM"),
        TGA_TARGA_IMAGE_FILE_FORMAT("TGA"),
        OGCAPI("OGCAPI"),

        HIERARCHICAL_DATA_FORMAT_RELEASE_5("HDF5"),
        HIERARCHICAL_DATA_FORMAT_RELEASE_5_IMAGE("HDF5Image"),
        USGS_ASTROGEOLOGY_ISIS_CUBE("ISIS3"),
        USGS_ASTROGEOLOGY_ISIS_CUBE_2("ISIS2"),
        NASA_PLANETARY_DATA_SYSTEM("PDS"),
        NASA_PLANETARY_DATA_SYSTEM_4("PDS4"),
        MIPL_VICAR_FILE("VICAR"),
        EARTHWATCH_TIL("TIL"),
        ERDAS_IMAGINE_RAW("EIR"),
        DIPEX("DIPEx"),
        FARSITE_V_4_LANDSCAPE_FILE("LCP"),
        NOAA_VERTICAL_DATUM("GTX"),
        NADCON_LOS_LAS_DATUM_GRID_SHIFT("LOSLAS"),
        NTV2("NTv2"),
        CTABLE2("CTable2"),
        ACE2("ACE2"),
        SNODAS("SNODAS"),
        KOLOR_RAW("KRO"),
        ROI_PAC("ROI_PAC"),
        R_RASTER("R"),
        NATURAL_RESOURCES_CANADA_S_GEOID("BYN"),
        NOAA_GEOCON_NADCON5("NOAA_B"),
        NSIDC_SEA_ICE_CONCENTRATIONS_BINARY("NSIDCbin"),
        SWEDISH_GRID_RIK("RIK"),
        USGS_OPTIONAL_ASCII_DEM("USGSDEM"),
        GEOSOFT_GRID_EXCHANGE_FORMAT("GXF"),
        BATHYMETRY_ATTRIBUTED_GRID("BAG"),
        S_102_BATHYMETRIC_SURFACE_PRODUCT("S102"),
        NORTHWOOD_NUMERIC_GRID_FORMAT("NWT_GRD"),
        NORTHWOOD_CLASSIFIED_GRID_FORMAT("NWT_GRC"),
        ARC_DIGITIZED_RASTER_GRAPHICS("ADRG"),
        STANDARD_RASTER_PRODUCT("SRP"),
        MAGELLAN_TOPO("BLX"),
        GEOPACKAGE("GPKG"),
        ESRI_FILE_GDB("OpenFileGDB"),
        AUTO_CAD("CAD"),
        PLANET_LABS_SCENES_API("PLSCENES"),
        NEXT_GIS_WEB("NGW"),
        GENERIC_BINARY("GenBin"),
        ENVI("ENVI"),
        ESRI("EHdr"),
        ISCE("ISCE"),
        ZARR("Zarr"),
        HTTP("HTTP");

        private final String shortName;

        FileConverterOutputFormat(final String outputFormat) {
            this.shortName = outputFormat;
        }
    }

    public static class FileConverterOptionsBuilder {
        private Collection<String> srcNoDataValues;
        private Collection<String> destinationNoDataValues;
        private int width;
        private int height;
        private FileConverterResamplingMethod resamplingMethod;
        private Proj4Declaration targetSpatialReference;
        private FileConverterOutputFormat outputFormat;
        private Path sourceFile;
        private Path targetFile;

        private FileConverterOptionsBuilder() {}

        public FileConverterOptionsBuilder srcNoDataValues(
                final Collection<String> srcNoDataValues) {
            this.srcNoDataValues = srcNoDataValues;
            return this;
        }

        public FileConverterOptionsBuilder destinationNoDataValues(
                final Collection<String> destinationNoDataValues) {
            this.destinationNoDataValues = destinationNoDataValues;
            return this;
        }

        public FileConverterOptionsBuilder width(final int width) {
            this.width = width;
            return this;
        }

        public FileConverterOptionsBuilder height(final int height) {
            this.height = height;
            return this;
        }

        public FileConverterOptionsBuilder resamplingMethod(
                final FileConverterResamplingMethod resamplingMethod) {
            this.resamplingMethod = resamplingMethod;
            return this;
        }

        public FileConverterOptionsBuilder targetSpatialReference(
                final Proj4Declaration targetSpatialReference) {
            this.targetSpatialReference = targetSpatialReference;
            return this;
        }

        public FileConverterOptionsBuilder outputFormat(
                final FileConverterOutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public FileConverterOptionsBuilder sourceFile(final Path sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public FileConverterOptionsBuilder targetFile(final Path targetFile) {
            this.targetFile = targetFile;
            return this;
        }

        public GdalWarpOptions build() {
            if (sourceFile == null) {
                throw new NullPointerException("The property \"sourceFile\" is null. ");
            }
            if (targetFile == null) {
                throw new NullPointerException("The property \"targetFile\" is null. ");
            }
            return new GdalWarpOptions(
                    srcNoDataValues,
                    destinationNoDataValues,
                    width,
                    height,
                    resamplingMethod,
                    targetSpatialReference,
                    outputFormat,
                    sourceFile,
                    targetFile);
        }
    }
}
