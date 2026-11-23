package ch.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public record Layer(
        @Nullable String layer,
        @Nullable Integer count,
        @Nullable String geometry,
        @Nullable Integer attributeCount,
        @Nullable List<@Valid Attribute> attributes) {}
