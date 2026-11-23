package ch.uetlib.geo.kml.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Placemark(
        @Nullable String name,
        @Nullable String description,
        @Nullable String styleId,
        @Nullable @Valid Geometry geometry,
        Map<String, String> extendedData) {}
