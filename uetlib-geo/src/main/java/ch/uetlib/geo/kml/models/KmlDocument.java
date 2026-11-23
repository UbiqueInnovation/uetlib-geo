package ch.uetlib.geo.kml.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;

import java.util.Collection;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KmlDocument(
        Collection<@Valid Placemark> placemarks,
        Map<String, StyleMap> styleMaps,
        Map<String, Style> styles) {}
