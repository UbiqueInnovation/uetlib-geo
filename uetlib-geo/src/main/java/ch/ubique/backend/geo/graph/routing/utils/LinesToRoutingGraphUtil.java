package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;

import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LinesToRoutingGraphUtil<T> {

    private final Collection<LineWithInfo<T>> lines;
    private final LinesToGraphConfig<T> config;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LinesToRoutingGraphUtil(
            final LinesToGraphConfig<T> config, final Collection<LineWithInfo<T>> lines) {
        this.lines = lines;
        this.config = config;
    }

    public LinesToRoutingGraphUtil(
            final Collection<LineString> lines, final LinesToGraphConfig<T> config) {
        //noinspection unchecked
        this.lines =
                lines.stream()
                        .map(line -> new LineWithInfo<>(line, (T) line.getUserData()))
                        .toList();
        this.config = config;
    }

    public static <S> LinesToRoutingGraphUtil<S> withInfo(
            final Collection<LineWithInfo<S>> lines, final LinesToGraphConfig<S> config) {
        return new LinesToRoutingGraphUtil<>(config, lines);
    }

    public RoutingGraphs buildGraph() {

        if (lines.isEmpty()) {
            logger.info("No paths provided, returning empty graph.");
            return RoutingGraphs.empty();
        }

        logger.info("Creating complete graph.");
        final var completeGraph = LinesToGraphUtil.buildGraph(lines, config);
        logger.info("Created complete graph {}.", completeGraph);

        logger.info("Creating routing graph.");
        final var routingGraph = RoutingGraphUtil.createRoutingGraph(completeGraph);
        logger.info("Created routing graph {}.", routingGraph);

        return new RoutingGraphs(completeGraph, routingGraph);
    }
}
