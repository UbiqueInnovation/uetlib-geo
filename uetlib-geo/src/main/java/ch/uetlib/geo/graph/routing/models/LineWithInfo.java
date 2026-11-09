package ch.uetlib.geo.graph.routing.models;

import org.locationtech.jts.geom.LineString;

public record LineWithInfo<T>(LineString line, T info) {}
