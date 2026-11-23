package ch.uetlib.geo.graph.utils;

import static ch.uetlib.geo.coordinate.constants.CoordinateSystems.WGS84;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNullElse;

import ch.uetlib.core.utils.IndeterminateProgressLogger;
import ch.uetlib.core.utils.ProgressLogger;
import ch.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.uetlib.geo.coordinate.util.JtsConverter;
import ch.uetlib.geo.coordinate.util.JtsUtil;
import ch.uetlib.geo.graph.models.Edge;
import ch.uetlib.geo.graph.models.Graph;
import ch.uetlib.geo.graph.models.Vertex;
import ch.uetlib.geo.graph.routing.models.LineWithInfo;
import ch.uetlib.geo.graph.routing.utils.Snapping3dPointIndex;
import ch.uetlib.geo.spatial.TypedStrTree;
import ch.uetlib.geo.utils.InterpolationUtils;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LinesGraphBuilder<E> {

    private static final Logger logger = LoggerFactory.getLogger(LinesGraphBuilder.class);

    private final boolean isDirected;
    private final Graph<Coordinate, E> graph;
    private final Snapping3dPointIndex snapping3dPointIndex;
    private final Map<VertexKey, Vertex<Coordinate, E>> coordinatesToVertex;
    private final IndeterminateProgressLogger vertexProgressLogger;
    private final IndeterminateProgressLogger edgeProgressLogger;
    private final LineIntersector lineIntersector;
    private final double snapTolerance;
    private final ToIntFunction<E> levelExtractor;
    private @Nullable CoordinateReferenceSystem crs;

    public LinesGraphBuilder(final double snapTolerance) {
        this(snapTolerance, Graph.DEFAULT_GRAPH_ID);
    }

    public LinesGraphBuilder(final double snapTolerance, final long graphId) {
        this(snapTolerance, graphId, null);
    }

    public LinesGraphBuilder(final double snapTolerance, final ToIntFunction<E> levelExtractor) {
        this(snapTolerance, Graph.DEFAULT_GRAPH_ID, levelExtractor);
    }

    public LinesGraphBuilder(
            final double snapTolerance,
            final long graphId,
            @Nullable final ToIntFunction<E> levelExtractor) {
        this(false, snapTolerance, graphId, levelExtractor);
    }

    public LinesGraphBuilder(
            final boolean isDirected,
            final double snapTolerance,
            final long graphId,
            final @Nullable ToIntFunction<E> levelExtractor) {
        this.isDirected = isDirected;
        this.snapTolerance = snapTolerance;
        this.levelExtractor = requireNonNullElse(levelExtractor, ignored -> 0);
        this.graph = new Graph<>(graphId);
        this.snapping3dPointIndex = new Snapping3dPointIndex(snapTolerance);
        this.coordinatesToVertex = new HashMap<>();
        this.lineIntersector = new RobustLineIntersector();

        final var mdc = MDC.getCopyOfContextMap();
        vertexProgressLogger =
                new IndeterminateProgressLogger("Inserting vertices: ", 1_000_000, mdc);
        edgeProgressLogger = new IndeterminateProgressLogger("Inserting edges: ", 1_000_000, mdc);
    }

    public Graph<Coordinate, E> build() {
        mergeVerticesAtLevelSwitch();
        return graph;
    }

    public <C extends ch.uetlib.geo.coordinate.models.Coordinate> Graph<C, E> build(
            final CoordinateSystem<C> targetCrs) {
        final var sourceConverter = JtsConverter.forCrs(requireNonNullElse(crs, WGS84.crs()));
        final var targetConverter = JtsConverter.forClass(targetCrs.coordinateClass());
        return build().map(
                        vertex ->
                                targetConverter.transform(
                                        sourceConverter.toBackendLib(vertex.getProperties())),
                        Edge::getProperties);
    }

    public LinesGraphBuilder<E> addLine(final LineString lineString, final E info) {
        final var lineCrs = JtsUtil.getCrs(lineString);
        if (crs == null) {
            crs = lineCrs;
        }
        if (!Objects.equals(crs, lineCrs)) {
            throw new IllegalArgumentException(
                    "Building graph for " + crs + " but got line with " + lineCrs);
        }

        final var level = levelExtractor.applyAsInt(info);
        final var coordinates = lineString.getCoordinateSequence();
        IntStream.range(1, coordinates.size())
                .forEach(i -> {
                    final var c0 = coordinates.getCoordinate(i - 1);
                    final var c1 = coordinates.getCoordinate(i);

                    final var v0 = getOrCreateVertex(c0, level);
                    final var v1 = getOrCreateVertex(c1, level);

                    upsertDirectedEdge(v0, v1, info);
                    if (!isDirected) {
                        upsertDirectedEdge(v1, v0, info);
                    }
                });
        return this;
    }

    public LinesGraphBuilder<E> addLine(final LineWithInfo<E> lineWithInfo) {
        return addLine(lineWithInfo.line(), lineWithInfo.info());
    }

    public LinesGraphBuilder<E> addLines(final Stream<LineWithInfo<E>> lines) {
        lines.forEach(this::addLine);
        return this;
    }

    public LinesGraphBuilder<E> addLines(final Collection<LineWithInfo<E>> lines) {
        return addLines(lines.stream());
    }

    public LinesGraphBuilder<E> createVerticesForIntersections() {
        logger.info("Finding intersecting edges.");
        final var spatialIndex = new TypedStrTree<>(graph.getDirectedEdges(), this::envelope);

        final var intersectionCounter = new AtomicInteger();
        final var progressLogger = new ProgressLogger(graph.getDirectedEdges().size());

        for (final var edge : graph.getDirectedEdges().stream().toList()) {

            final var queryEnvelope = envelope(edge);
            queryEnvelope.expandBy(snapTolerance);

            spatialIndex.query(queryEnvelope).stream()
                    .map(e -> findIntersection(edge, e))
                    .flatMap(Optional::stream)
                    .findAny()
                    .ifPresent(coordinate -> {
                        splitEdge(edge, coordinate);
                        intersectionCounter.incrementAndGet();
                    });

            progressLogger.step();
        }

        logger.info(
                "Found {} intersections. Added all of them to the graph.",
                intersectionCounter.get());

        return this;
    }

    private void splitEdge(final Edge<Coordinate, E> edge, final Coordinate coordinate) {
        logger.debug("Splitting edge {} at coordinate {}", edge, coordinate);
        final var level = levelExtractor.applyAsInt(edge.getProperties());
        final var intersectionVertex = getOrCreateVertex(coordinate, level);
        graph.removeEdge(edge);
        upsertDirectedEdge(edge.getFrom(), intersectionVertex, edge.getProperties());
        upsertDirectedEdge(intersectionVertex, edge.getTo(), edge.getProperties());
    }

    private Optional<Coordinate> findIntersection(
            final Edge<Coordinate, E> a, final Edge<Coordinate, E> b) {
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
        return Math.abs(a - b) > snapTolerance;
    }

    private Optional<Coordinate> findIntersectionNearVertex(
            final Edge<Coordinate, E> edge, final Vertex<Coordinate, E> vertex) {

        final var coordinate = vertex.getProperties();

        if (coordinate.distance(edge.getU().getProperties()) <= snapTolerance
                || coordinate.distance(edge.getV().getProperties()) <= snapTolerance) {
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

    private LineSegment toSegment(final Edge<Coordinate, E> edge) {
        return new LineSegment(edge.getFrom().getProperties(), edge.getTo().getProperties());
    }

    private void upsertDirectedEdge(
            final Vertex<Coordinate, E> u, final Vertex<Coordinate, E> v, final E info) {
        if (u.equals(v)) {
            return;
        }
        graph.getEdge(u, v)
                .ifPresentOrElse(
                        existing -> {
                            if (!Objects.equals(existing.getProperties(), info)) {
                                createEdge(u, v, info); // Only create edge if properties differ
                            }
                        },
                        () -> createEdge(u, v, info));
    }

    private void createEdge(
            final Vertex<Coordinate, E> u, final Vertex<Coordinate, E> v, final E info) {
        graph.addDirectedEdge(u, v, info);
        edgeProgressLogger.step();
    }

    private Vertex<Coordinate, E> getOrCreateVertex(final Coordinate coordinate, final int level) {
        final var key = snappedVertexKey(coordinate, level);
        return coordinatesToVertex.computeIfAbsent(key, ignored -> createVertex(coordinate));
    }

    private VertexKey snappedVertexKey(final Coordinate coordinate, final int level) {
        if (snapTolerance == 0) {
            return new VertexKey(coordinate, level);
        }
        return new VertexKey(snapping3dPointIndex.snap(coordinate), level);
    }

    private Vertex<Coordinate, E> createVertex(final Coordinate coordinate) {
        final var vertex = graph.createVertex(coordinate);
        vertexProgressLogger.step();
        return vertex;
    }

    private Envelope envelope(final Edge<Coordinate, E> edge) {
        return new Envelope(edge.getU().getProperties(), edge.getV().getProperties());
    }

    private @Nullable Coordinate intersection(
            final Edge<Coordinate, E> a, final Edge<Coordinate, E> b) {
        lineIntersector.computeIntersection(
                a.getFrom().getProperties(),
                a.getTo().getProperties(),
                b.getFrom().getProperties(),
                b.getTo().getProperties());
        if (lineIntersector.hasIntersection()) {
            return lineIntersector.getIntersection(0);
        }
        return null;
    }

    private void mergeVerticesAtLevelSwitch() {
        logger.info("Merging vertices at level switch (e.g. at the end of a bridge)");
        final var candidates =
                graph.getVertices().stream()
                        .collect(Collectors.groupingBy(Vertex::getProperties))
                        .values()
                        .stream()
                        .filter(list -> list.size() > 1)
                        .toList();

        logger.info("Found {} candidates. Going to merge now.", candidates.size());
        int counter = 0;
        for (final var mergeCandidate : candidates) {
            final var main =
                    mergeCandidate.stream().max(comparingInt(Vertex::degree)).orElseThrow();
            final var verticesToMerge =
                    mergeCandidate.stream()
                            .filter(v -> !v.equals(main))
                            .filter(v -> v.degree() <= 1) // Only merge at endpoints
                            .toList();
            for (final var vertexToMerge : verticesToMerge) {
                vertexToMerge
                        .outgoingEdges()
                        .forEach(e -> graph.addDirectedEdge(main, e.getTo(), e.getProperties()));
                vertexToMerge
                        .incomingEdges()
                        .forEach(e -> graph.addDirectedEdge(e.getFrom(), main, e.getProperties()));
                graph.removeVertex(vertexToMerge);
                ++counter;
            }
        }
        logger.info("Merged {} vertices.", counter);
    }

    private record VertexKey(double x, double y, Double z, int level) {

        VertexKey(final Coordinate coordinate, final int level) {
            this(coordinate.x, coordinate.y, coordinate.getZ(), level);
        }
    }
}
