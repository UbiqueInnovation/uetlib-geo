package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.core.utils.ProgressLogger;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.spatial.TypedQuadtree;
import ch.ubique.backend.geo.utils.InterpolationUtils;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LinesToGraphUtil<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Collection<LineWithInfo<T>> lines;
    private final LinesToGraphConfig<T> config;

    private final Graph<RoutingGraphVertexProperties, Weight> graph;
    private final Snapping3dPointIndex snapping3dPointIndex;
    private final Map<Coordinate, Vertex<RoutingGraphVertexProperties, Weight>> coordinatesToVertex;

    private final LineIntersector lineIntersector;

    public LinesToGraphUtil(
            @NotNull final Collection<LineWithInfo<T>> lines,
            @NotNull final LinesToGraphConfig<T> config) {
        this.config = config;
        this.lines = lines;
        this.graph = new Graph<>();
        this.snapping3dPointIndex = new Snapping3dPointIndex(config.snapTolerance());
        this.coordinatesToVertex = new HashMap<>();
        lineIntersector = new RobustLineIntersector();
    }

    public static <S> Graph<RoutingGraphVertexProperties, Weight> buildGraph(
            @NotNull final Collection<LineWithInfo<S>> lines,
            @NotNull final LinesToGraphConfig<S> config) {
        final var builder = new LinesToGraphUtil<>(lines, config);
        builder.createGraph();
        builder.findIntersections();
        return builder.graph;
    }

    private void createGraph() {
        final var sortedLines = sortedLines();
        final var progressLogger = new ProgressLogger("Creating graph: ", sortedLines.size());
        for (final var lineWithInfo : sortedLines) {
            final var line = lineWithInfo.line();
            final var info = lineWithInfo.info();
            final var isHighway = config.isHighway().test(info);

            for (final var segment : JtsUtil.segments(line)) {
                final var v0 = getOrCreateVertex(segment.p0, isHighway);
                final var v1 = getOrCreateVertex(segment.p1, isHighway);

                upsertEdge(v0, v1, info);
                upsertEdge(v1, v0, info);
            }
            progressLogger.step();
        }
    }

    private void findIntersections() {
        logger.info("Finding intersecting edges.");
        final var quadTree = new TypedQuadtree<>(graph.getDirectedEdges(), this::envelope);

        final var progressLogger = new ProgressLogger(graph.getDirectedEdges().size());

        for (final var edge : graph.getDirectedEdges().stream().toList()) {

            final var queryEnvelope = envelope(edge);
            queryEnvelope.expandBy(config.snapTolerance());

            final var intersectionMaybe =
                    quadTree.query(queryEnvelope).stream()
                            .map(e -> findIntersection(edge, e))
                            .flatMap(Optional::stream)
                            .findAny();

            intersectionMaybe.ifPresent(coordinate -> splitEdge(edge, coordinate));
            progressLogger.step();
        }
    }

    private void splitEdge(
            final Edge<RoutingGraphVertexProperties, Weight> edge, final Coordinate coordinate) {
        final var intersectionVertex = getOrCreateVertex(coordinate, isHighway(edge));

        logger.debug("Splitting edge {} at coordinate {}", edge, coordinate);

        graph.removeEdge(edge);

        final var weight = edge.getProperties().weight();
        final var t = (float) toSegment(edge).projectionFactor(coordinate);

        upsertEdge(edge.getFrom(), intersectionVertex, t * weight);
        upsertEdge(intersectionVertex, edge.getTo(), (1 - t) * weight);
    }

    private Optional<Coordinate> findIntersection(
            final Edge<RoutingGraphVertexProperties, Weight> a,
            final Edge<RoutingGraphVertexProperties, Weight> b) {
        if (a.hasCommonVertex(b)) {
            return Optional.empty();
        }

        final var intersection = intersection(a, b);
        if (intersection == null) {
            return findIntersectionNearVertex(a, b.getU())
                    .or(() -> findIntersectionNearVertex(a, b.getV()));
        }

        final var aSegment = toSegment(a);
        final var bSegment = toSegment(b);

        final var zA = InterpolationUtils.zInterpolate(intersection, aSegment);
        final var zB = InterpolationUtils.zInterpolate(intersection, bSegment);

        if (elevationDifferenceOutsideSnapTolerance(zA, zB)) {
            return Optional.empty();
        }

        return Optional.of(intersection);
    }

    private boolean elevationDifferenceOutsideSnapTolerance(final double a, final double b) {
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return false;
        }
        return Math.abs(a - b) > config.snapTolerance();
    }

    private Optional<Coordinate> findIntersectionNearVertex(
            final Edge<RoutingGraphVertexProperties, Weight> edge,
            final Vertex<RoutingGraphVertexProperties, Weight> vertex) {

        final var coordinate = vertex.getProperties().coordinate();
        final var snapTolerance = config.snapTolerance();

        if (coordinate.distance(edge.getU().getProperties().coordinate()) <= snapTolerance
                || coordinate.distance(edge.getV().getProperties().coordinate()) <= snapTolerance) {
            return Optional.empty();
        }

        final var segment = toSegment(edge);
        final var distanceToSegment = segment.distance(coordinate);
        if (distanceToSegment > snapTolerance) {
            return Optional.empty();
        }

        final var intersection = segment.project(coordinate);

        final var z = InterpolationUtils.zInterpolate(segment.project(intersection), segment);

        if (elevationDifferenceOutsideSnapTolerance(coordinate.getZ(), z)) {
            return Optional.empty();
        }

        return Optional.of(intersection);
    }

    private LineSegment toSegment(final Edge<RoutingGraphVertexProperties, Weight> edge) {
        return new LineSegment(
                edge.getFrom().getProperties().coordinate(),
                edge.getTo().getProperties().coordinate());
    }

    private void upsertEdge(
            final Vertex<RoutingGraphVertexProperties, Weight> u,
            final Vertex<RoutingGraphVertexProperties, Weight> v,
            final T info) {
        upsertEdge(u, v, weight(u, v, info));
    }

    private void upsertEdge(
            final Vertex<RoutingGraphVertexProperties, Weight> u,
            final Vertex<RoutingGraphVertexProperties, Weight> v,
            final float weight) {

        if (u.equals(v)) {
            return;
        }

        graph.getEdge(u, v)
                .ifPresentOrElse(
                        existing -> {
                            if (weight < existing.getProperties().weight()) {
                                existing.setProperties(new Weight(weight));
                            }
                        },
                        () -> graph.addDirectedEdge(u, v, new Weight(weight)));
    }

    private Vertex<RoutingGraphVertexProperties, Weight> getOrCreateVertex(
            final Coordinate coordinate, final boolean isHighway) {

        return coordinatesToVertex.computeIfAbsent(
                snapping3dPointIndex.snap(coordinate),
                c -> graph.createVertex(new RoutingGraphVertexProperties(c, isHighway)));
    }

    private List<LineWithInfo<T>> sortedLines() {
        final var priorityComparator =
                Comparator.comparingDouble(config.snappingPriority()).reversed();

        return lines.stream()
                .sorted(Comparator.comparing(LineWithInfo::info, priorityComparator))
                .toList();
    }

    private Envelope envelope(final Edge<RoutingGraphVertexProperties, ?> edge) {
        return new Envelope(
                edge.getU().getProperties().coordinate(), edge.getV().getProperties().coordinate());
    }

    private static boolean isHighway(final Edge<RoutingGraphVertexProperties, ?> edge) {
        return edge.getU().getProperties().isHighway() && edge.getV().getProperties().isHighway();
    }

    private float weight(
            final Vertex<RoutingGraphVertexProperties, Weight> u,
            final Vertex<RoutingGraphVertexProperties, Weight> v,
            final T info) {
        final var cU = u.getProperties().coordinate();
        final var cV = v.getProperties().coordinate();
        return config.weightingFunction().computeWeight(cU, cV, info);
    }

    public Coordinate intersection(
            final Edge<RoutingGraphVertexProperties, Weight> a,
            final Edge<RoutingGraphVertexProperties, Weight> b) {
        lineIntersector.computeIntersection(
                a.getFrom().getProperties().coordinate(),
                a.getTo().getProperties().coordinate(),
                b.getFrom().getProperties().coordinate(),
                b.getTo().getProperties().coordinate());
        if (lineIntersector.hasIntersection()) {
            return lineIntersector.getIntersection(0);
        }
        return null;
    }
}
