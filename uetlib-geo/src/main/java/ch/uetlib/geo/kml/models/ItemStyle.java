package ch.uetlib.geo.kml.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemStyle(
        @Nullable String color,
        @Nullable Double scale,
        @Nullable Double width,
        @Nullable @Valid Icon icon) {}
