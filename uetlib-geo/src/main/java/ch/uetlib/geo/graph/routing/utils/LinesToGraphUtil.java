package ch.uetlib.geo.graph.routing.utils;

import ch.uetlib.core.utils.ProgressLogger;
import ch.uetlib.geo.coordinate.util.JtsUtil;
import ch.uetlib.geo.graph.models.Edge;
import ch.uetlib.geo.graph.models.Graph;
import ch.uetlib.geo.graph.models.Vertex;
import ch.uetlib.geo.graph.routing.models.LineWithInfo;
import ch.uetlib.geo.graph.routing.models.RoutingCompleteEdge;
import ch.uetlib.geo.graph.routing.models.RoutingVertex;
import ch.uetlib.geo.spatial.TypedStrTree;
import ch.uetlib.geo.utils.InterpolationUtils;

import org.jspecify.annotations.Nullable;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LinesToGraphUtil<T> {

    private static final Logger logger = LoggerFactory.getLogger(LinesToGraphUtil.class);

    private final Collection<LineWithInfo<T>> lines;
    private final LinesToGraphConfig<T> config;

    private final Graph<RoutingVertex, RoutingCompleteEdge> graph;
    private final Snapping3dPointIndex snapping3dPointIndex;
    private final Map<Coordinate, Vertex<RoutingVertex, RoutingCompleteEdge>> coordinatesToVertex;

    private final LineIntersector lineIntersector;

    private LinesToGraphUtil(
            final Collection<LineWithInfo<T>> lines, final LinesToGraphConfig<T> config) {
        this.config = config;
        this.lines = lines;
        this.graph = new Graph<>();
        this.snapping3dPointIndex = new Snapping3dPointIndex(config.snapTolerance());
        this.coordinatesToVertex = new HashMap<>();
        lineIntersector = new RobustLineIntersector();
    }

    public static <S> Graph<RoutingVertex, RoutingCompleteEdge> buildGraph(
            final Collection<LineWithInfo<S>> lines, final LinesToGraphConfig<S> config) {
        final var builder = new LinesToGraphUtil<>(lines, config);
        builder.createGraph();
        builder.findIntersections();
        return builder.graph;
    }

    private static boolean isHighway(final Edge<RoutingVertex, ?> edge) {
        return edge.getU().getProperties().isHighway() && edge.getV().getProperties().isHighway();
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
        final var spatialIndex = new TypedStrTree<>(graph.getDirectedEdges(), this::envelope);

        final var progressLogger = new ProgressLogger(graph.getDirectedEdges().size());

        for (final var edge : graph.getDirectedEdges().stream().toList()) {

            final var queryEnvelope = envelope(edge);
            queryEnvelope.expandBy(config.snapTolerance());

            final var intersectionMaybe =
                    spatialIndex.query(queryEnvelope).stream()
                            .map(e -> findIntersection(edge, e))
                            .flatMap(Optional::stream)
                            .findAny();

            intersectionMaybe.ifPresent(coordinate -> splitEdge(edge, coordinate));
            progressLogger.step();
        }
    }

    private void splitEdge(
            final Edge<RoutingVertex, RoutingCompleteEdge> edge, final Coordinate coordinate) {
        final var intersectionVertex = getOrCreateVertex(coordinate, isHighway(edge));

        logger.debug("Splitting edge {} at coordinate {}", edge, coordinate);

        graph.removeEdge(edge);

        final var edgeProperties = edge.getProperties();
        final var weight = edgeProperties.weight();
        final var t = (float) toSegment(edge).projectionFactor(coordinate);

        upsertEdge(edge.getFrom(), intersectionVertex, edgeProperties.withWeight(t * weight));
        upsertEdge(intersectionVertex, edge.getTo(), edgeProperties.withWeight((1 - t) * weight));
    }

    private Optional<Coordinate> findIntersection(
            final Edge<RoutingVertex, RoutingCompleteEdge> a,
            final Edge<RoutingVertex, RoutingCompleteEdge> b) {
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
            final Edge<RoutingVertex, RoutingCompleteEdge> edge,
            final Vertex<RoutingVertex, RoutingCompleteEdge> vertex) {

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

    private LineSegment toSegment(final Edge<RoutingVertex, RoutingCompleteEdge> edge) {
        return new LineSegment(
                edge.getFrom().getProperties().coordinate(),
                edge.getTo().getProperties().coordinate());
    }

    private void upsertEdge(
            final Vertex<RoutingVertex, RoutingCompleteEdge> u,
            final Vertex<RoutingVertex, RoutingCompleteEdge> v,
            final T info) {
        upsertEdge(u, v, weight(u, v, info));
    }

    private void upsertEdge(
            final Vertex<RoutingVertex, RoutingCompleteEdge> u,
            final Vertex<RoutingVertex, RoutingCompleteEdge> v,
            final RoutingCompleteEdge edgeInfo) {

        if (u.equals(v)) {
            return;
        }

        graph.getEdge(u, v)
                .ifPresentOrElse(
                        existing -> {
                            if (edgeInfo.weight() < existing.getProperties().weight()) {
                                existing.setProperties(edgeInfo);
                            }
                        },
                        () -> graph.addDirectedEdge(u, v, edgeInfo));
    }

    private Vertex<RoutingVertex, RoutingCompleteEdge> getOrCreateVertex(
            final Coordinate coordinate, final boolean isHighway) {

        final var vertex =
                coordinatesToVertex.computeIfAbsent(
                        snapping3dPointIndex.snap(coordinate),
                        c -> graph.createVertex(new RoutingVertex(c, isHighway)));

        // Make sure isHighway is set, if vertex already existed but with !isHighway
        vertex.setProperties(vertex.getProperties().update(isHighway));

        return vertex;
    }

    private List<LineWithInfo<T>> sortedLines() {
        final var priorityComparator =
                Comparator.comparingDouble(config.snappingPriority()).reversed();

        return lines.stream()
                .sorted(Comparator.comparing(LineWithInfo::info, priorityComparator))
                .toList();
    }

    private Envelope envelope(final Edge<RoutingVertex, ?> edge) {
        return new Envelope(
                edge.getU().getProperties().coordinate(), edge.getV().getProperties().coordinate());
    }

    private RoutingCompleteEdge weight(
            final Vertex<RoutingVertex, RoutingCompleteEdge> u,
            final Vertex<RoutingVertex, RoutingCompleteEdge> v,
            final T info) {
        final var cU = u.getProperties().coordinate();
        final var cV = v.getProperties().coordinate();
        return config.routingEdgeFunction().toRoutingEdge(cU, cV, info);
    }

    private @Nullable Coordinate intersection(
            final Edge<RoutingVertex, RoutingCompleteEdge> a,
            final Edge<RoutingVertex, RoutingCompleteEdge> b) {
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
