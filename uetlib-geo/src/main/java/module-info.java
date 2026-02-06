import org.jspecify.annotations.NullMarked;

@NullMarked
open module uetlib.geo {
    requires artifact.version.service;
    requires com.github.benmanes.caffeine;
    requires com.google.common;
    requires com.pivovarit.function;
    requires io.swagger.v3.oas.annotations;
    requires jakarta.validation;
    requires java.desktop;
    requires java.logging;
    requires java.xml;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.eclipse.emf.common;
    requires org.geotools.api;
    requires org.geotools.coverage;
    requires org.geotools.geopkg;
    requires org.geotools.geotiff;
    requires org.geotools.main;
    requires org.geotools.metadata;
    requires org.geotools.referencing;
    requires org.geotools.render;
    requires org.geotools.shapefile;
    requires org.jspecify;
    requires org.locationtech.jts;
    requires org.slf4j;
    requires osmosis.core;
    requires osmosis.pbf2;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires uetlib.core;
    requires com.google.protobuf;
}
