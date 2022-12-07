package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinesToRoutingGraphUtil<T> {

    private static final double DEFAULT_SNAP_TOLERANCE = 1;

    private final Collection<LineString> lines;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private double snapTolerance = DEFAULT_SNAP_TOLERANCE;
    private ToDoubleFunction<T> snappingPriority;
    private ToDoubleFunction<T> edgePriority;

    public LinesToRoutingGraphUtil(final Collection<LineString> lines) {
        this.lines = lines;
    }

    public static <S> LinesToRoutingGraphUtil<S> withInfo(
            final Collection<LineWithInfo<S>> linesWithInfo) {
        return new LinesToRoutingGraphUtil<>(
                linesWithInfo.stream()
                        .map(
                                lineWithInfo -> {
                                    lineWithInfo.line().setUserData(lineWithInfo.info());
                                    return lineWithInfo.line();
                                })
                        .toList());
    }

    public LinesToRoutingGraphUtil<T> snapTolerance(final double snapTolerance) {
        this.snapTolerance = snapTolerance;
        return this;
    }

    /**
     * Shortcut for {@link LinesToRoutingGraphUtil#snappingPriority}.Lines that match the predicate
     * have priority over lines, that do not match it.
     */
    public LinesToRoutingGraphUtil<T> hasSnappingPriority(final Predicate<T> hasPriority) {
        return snappingPriority(x -> hasPriority.test(x) ? 1 : 0);
    }

    /**
     * A higher priority is better. I.e. vertices from lines of higher priority are preferred for
     * snapping
     */
    public LinesToRoutingGraphUtil<T> snappingPriority(
            final ToDoubleFunction<T> priorityExtractor) {
        this.snappingPriority = priorityExtractor;
        return this;
    }

    /**
     * Shortcut for {@link LinesToRoutingGraphUtil#edgePriority}.Lines that match the predicate have
     * priority over lines, that do not match it.
     */
    public LinesToRoutingGraphUtil<T> hasEdgePriority(final Predicate<T> hasPriority) {
        return edgePriority(x -> hasPriority.test(x) ? 1 : 0);
    }

    /**
     * A higher priority is better. I.e. vertices from lines of higher priority are preferred for
     * when inserting edges between the same vertices.
     */
    public LinesToRoutingGraphUtil<T> edgePriority(final ToDoubleFunction<T> priorityExtractor) {
        this.edgePriority = priorityExtractor;
        return this;
    }

    public RoutingGraphs<T> buildGraph() {

        if (lines.isEmpty()) {
            logger.info("No paths provided, returning empty graph.");
            return RoutingGraphs.empty();
        }

        logger.info("Creating complete graph.");
        final var nodedSegmentStrings = computeNodedSegmentStrings(lines, snapTolerance);
        final var completeGraph = new Graph<Coordinate, T>();
        final var coordinateToVertex = createVertices(completeGraph, nodedSegmentStrings);
        createEdges(completeGraph, nodedSegmentStrings, coordinateToVertex);
        logger.info("Created complete graph {}.", completeGraph);

        logger.info("Creating routing graph.");
        final var routingGraph =
                buildRoutingGraph(lines, nodedSegmentStrings, completeGraph, coordinateToVertex);
        logger.info("Created routing graph {}.", routingGraph);

        return new RoutingGraphs<>(completeGraph, routingGraph);
    }

    private void createEdges(
            final Graph<Coordinate, T> graph,
            final List<LineString> lineStrings,
            final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex) {
        logger.info("Creating edges by linking neighbors of each path");
        for (final var lineString : lineStrings) {
            final var info = userData(lineString);
            JtsUtil.segments(lineString)
                    .forEach(
                            segment -> {
                                final var from = coordinateToVertex.get(segment.getCoordinate(0));
                                final var to = coordinateToVertex.get(segment.getCoordinate(1));
                                assert (!from.equals(to));
                                graph.getOrAddUndirectedEdge(from, to, info);
                            });
        }
    }

    private List<LineString> computeNodedSegmentStrings(
            final Collection<LineString> lines, final double snapTolerance) {
        logger.info("Creating segment strings for {} lines.", lines.size());
        final var segmentStrings =
                lines.stream()
                        .sorted(Comparator.comparing(this::userData, comparator(snappingPriority)))
                        .map(SegmentStringUtil::extractSegmentStrings)
                        .flatMap(Collection::stream)
                        .toList();

        // The noder merges nodes within snapTolerance and adds additional nodes for all line
        // intersections.
        final var noder = new SnappingNoder(snapTolerance);
        logger.info(
                "Noding segments using {} with snapTolerance {}", noder.getClass(), snapTolerance);
        noder.computeNodes(segmentStrings);
        //noinspection unchecked
        final var nodedSegmentStrings = (List<NodedSegmentString>) noder.getNodedSubstrings();

        final var factory = JtsUtil.factoryFromGeometryCollection(lines);
        return nodedSegmentStrings.stream()
                .map(
                        segmentString -> {
                            final var line =
                                    factory.createLineString(segmentString.getCoordinates());
                            line.setUserData(((Geometry) segmentString.getData()).getUserData());
                            return line;
                        })
                .sorted(Comparator.comparing(this::userData, comparator(edgePriority)))
                .distinct()
                .toList();
    }

    private Map<Coordinate, Vertex<Coordinate, T>> createVertices(
            final Graph<Coordinate, T> graph, final List<LineString> nodedSegmentStrings) {
        logger.info(
                "Getting distinct coordinates from {} noded segment strings",
                nodedSegmentStrings.size());
        final var coordinates =
                nodedSegmentStrings.stream()
                        .map(Geometry::getCoordinates)
                        .flatMap(Arrays::stream)
                        .distinct()
                        .toList();

        logger.info("Creating vertices for {} coordinates", coordinates.size());
        final var lastId = new AtomicInteger();
        return coordinates.stream()
                .collect(
                        Collectors.toMap(
                                coordinate -> coordinate,
                                coordinate ->
                                        graph.addVertex(lastId.incrementAndGet(), coordinate)));
    }

    @NotNull
    private Graph<Coordinate, RoutingGraphEdgeProperties<T>> buildRoutingGraph(
            final Collection<LineString> lines,
            final List<LineString> nodedSegmentStrings,
            final Graph<Coordinate, T> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex) {

        final var mergedLines = mergeSegmentStrings(lines, nodedSegmentStrings);
        final var routingGraph =
                mergedLinesToRoutingGraph(completeGraph, coordinateToVertex, mergedLines);
        removeDegreeTwoVertices(routingGraph, completeGraph);
        return routingGraph;
    }

    @NotNull
    private List<LineString> mergeSegmentStrings(
            final Collection<LineString> lines, final List<LineString> nodedSegmentStrings) {
        logger.info("Merging segment strings.");
        final var lineMerger = new LineMerger();
        lineMerger.add(nodedSegmentStrings);
        //noinspection unchecked
        return (List<LineString>) lineMerger.getMergedLineStrings();
    }

    @NotNull
    private Graph<Coordinate, RoutingGraphEdgeProperties<T>> mergedLinesToRoutingGraph(
            final Graph<Coordinate, T> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex,
            final List<LineString> mergedLines) {
        logger.info("Creating routing graph from {} merged lines.", mergedLines.size());

        final var routingGraph = new Graph<Coordinate, RoutingGraphEdgeProperties<T>>();
        for (final var line : mergedLines) {
            final var coordinates = line.getCoordinates();
            insertRoutingEdges(completeGraph, coordinateToVertex, routingGraph, line, coordinates);
        }
        return routingGraph;
    }

    private void removeDegreeTwoVertices(
            final Graph<Coordinate, RoutingGraphEdgeProperties<T>> routingGraph,
            final Graph<Coordinate, T> completeGraph) {
        new ArrayList<>(routingGraph.getVertices())
                .stream()
                        .filter(
                                vertex ->
                                        vertex.degree() > 1
                                                && completeGraph
                                                                .getVertex(vertex.getId())
                                                                .orElseThrow()
                                                                .degree()
                                                        == 2)
                        .forEach(vertex -> mergeEdgesAndRemoveVertex(vertex, routingGraph));
    }

    private void insertRoutingEdges(
            final Graph<Coordinate, T> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex,
            final Graph<Coordinate, RoutingGraphEdgeProperties<T>> routingGraph,
            final LineString line,
            final Coordinate[] coordinates) {
        final var first = coordinateToVertex.get(coordinates[0]);
        final var second = coordinateToVertex.get(coordinates[1]);
        final var firstEdge = completeGraph.getEdge(first, second).orElseThrow();

        final var last = coordinateToVertex.get(coordinates[coordinates.length - 1]);
        final var secondLast = coordinateToVertex.get(coordinates[coordinates.length - 2]);
        final var lastEdge = completeGraph.getEdge(last, secondLast).orElseThrow();

        // Special case: loop - skip for now
        if (first.equals(last)) {
            return;
        }

        final var distance = line.getLength();

        routingGraph.getOrAddVertex(first.getId(), first.getProperties());
        routingGraph.getOrAddVertex(last.getId(), last.getProperties());

        final var forwardsEdge = routingGraph.getOrAddDirectedEdge(first.getId(), last.getId());
        if (forwardsEdge.getProperties() == null
                || forwardsEdge.getProperties().distance() > distance) {
            forwardsEdge.setProperties(
                    new RoutingGraphEdgeProperties<>(distance, second, firstEdge.getProperties()));
        }

        final var backwardsEdge = routingGraph.getOrAddDirectedEdge(last.getId(), first.getId());
        if (backwardsEdge.getProperties() == null
                || backwardsEdge.getProperties().distance() > distance) {
            backwardsEdge.setProperties(
                    new RoutingGraphEdgeProperties<>(
                            distance, secondLast, lastEdge.getProperties()));
        }
    }

    private void mergeEdgesAndRemoveVertex(
            Vertex<Coordinate, RoutingGraphEdgeProperties<T>> vertex,
            final Graph<Coordinate, RoutingGraphEdgeProperties<T>> routingGraph) {

        if (vertex.degree() != 2) {
            logger.warn(
                    "You need to pass a vertex of degree 2. Got vertex of degree {}",
                    vertex.degree());
            return;
        }

        final var edges =
                vertex.getEdges().stream().map(Edge::getTwin).map(Optional::orElseThrow).toList();

        final var edgeA = edges.get(0);
        final var edgeB = edges.get(1);

        // Add direct edges
        final var forwards = routingGraph.getOrAddUndirectedEdge(edgeA.getFrom(), edgeB.getFrom());
        forwards.setProperties(
                new RoutingGraphEdgeProperties<>(
                        edgeA.getProperties().distance() + edgeB.getProperties().distance(),
                        edgeA.getProperties().direction(),
                        edgeA.getProperties().originalProperties()));

        final var backwards = routingGraph.getOrAddUndirectedEdge(edgeB.getFrom(), edgeA.getFrom());
        backwards.setProperties(
                new RoutingGraphEdgeProperties<>(
                        edgeA.getProperties().distance() + edgeB.getProperties().distance(),
                        edgeB.getProperties().direction(),
                        edgeB.getProperties().originalProperties()));

        // Remove old edges
        Stream.concat(vertex.getEdges().stream(), Stream.of(edgeA, edgeB))
                .forEach(routingGraph::removeEdge);

        // Remove vertex
        routingGraph.removeVertex(vertex);
    }

    private T userData(final Geometry geometry) {
        //noinspection unchecked
        return (T) geometry.getUserData();
    }

    @NotNull
    private Comparator<T> comparator(final @Nullable ToDoubleFunction<T> priorityExtractor) {
        if (priorityExtractor == null) {
            return Comparator.comparingInt(x -> 0);
        }
        return Comparator.comparingDouble(priorityExtractor).reversed();
    }
}
