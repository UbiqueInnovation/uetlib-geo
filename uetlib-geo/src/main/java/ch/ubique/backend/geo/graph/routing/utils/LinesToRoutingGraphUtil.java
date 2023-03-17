package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LinesToRoutingGraphUtil<T> {

    private static final double DEFAULT_SNAP_TOLERANCE = 1;

    private final Collection<LineString> lines;
    private final WeightingFunction<T> weightingFunction;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private double snapTolerance = DEFAULT_SNAP_TOLERANCE;
    private ToDoubleFunction<T> snappingPriority;
    private ToDoubleFunction<T> edgePriority;

    public LinesToRoutingGraphUtil(
            final Collection<LineString> lines, final WeightingFunction<T> weightingFunction) {
        this.lines = lines;
        this.weightingFunction = weightingFunction;
    }

    public static <S> LinesToRoutingGraphUtil<S> withInfo(
            final Collection<LineWithInfo<S>> linesWithInfo,
            final WeightingFunction<S> weightingFunction) {
        return new LinesToRoutingGraphUtil<>(
                linesWithInfo.stream()
                        .map(
                                lineWithInfo -> {
                                    lineWithInfo.line().setUserData(lineWithInfo.info());
                                    return lineWithInfo.line();
                                })
                        .toList(),
                weightingFunction);
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

    public RoutingGraphs buildGraph() {

        if (lines.isEmpty()) {
            logger.info("No paths provided, returning empty graph.");
            return RoutingGraphs.empty();
        }

        logger.info("Creating complete graph.");
        final var nodedSegmentStrings = computeNodedSegmentStrings(lines, snapTolerance);
        final var completeGraph = new Graph<Coordinate, Weight>();
        final var coordinateToVertex = createVertices(completeGraph, nodedSegmentStrings);
        createEdges(completeGraph, nodedSegmentStrings, coordinateToVertex);
        logger.info("Created complete graph {}.", completeGraph);

        logger.info("Creating routing graph.");
        final var routingGraph =
                buildRoutingGraph(lines, nodedSegmentStrings, completeGraph, coordinateToVertex);
        logger.info("Created routing graph {}.", routingGraph);

        return new RoutingGraphs(completeGraph, routingGraph);
    }

    private void createEdges(
            final Graph<Coordinate, Weight> graph,
            final List<LineString> lineStrings,
            final Map<Coordinate, Vertex<Coordinate, Weight>> coordinateToVertex) {
        logger.info("Creating edges by linking neighbors of each path");
        for (final var lineString : lineStrings) {
            final var info = userData(lineString);
            for (final var segment : JtsUtil.segments(lineString)) {
                final var fromCoordinate = segment.p0;
                final var toCoordinate = segment.p1;
                final var from = coordinateToVertex.get(fromCoordinate);
                final var to = coordinateToVertex.get(toCoordinate);
                assert (!from.equals(to));

                graph.getOrAddDirectedEdge(from, to, weight(fromCoordinate, toCoordinate, info));
                graph.getOrAddDirectedEdge(to, from, weight(toCoordinate, fromCoordinate, info));
            }
        }
    }

    @NotNull
    private Weight weight(final Coordinate from, final Coordinate to, final T info) {
        return new Weight(weightingFunction.computeWeight(from, to, info));
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

    private Map<Coordinate, Vertex<Coordinate, Weight>> createVertices(
            final Graph<Coordinate, Weight> graph, final List<LineString> nodedSegmentStrings) {
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
    private Graph<Coordinate, WeightAndDirection> buildRoutingGraph(
            final Collection<LineString> lines,
            final List<LineString> nodedSegmentStrings,
            final Graph<Coordinate, Weight> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, Weight>> coordinateToVertex) {

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
    private Graph<Coordinate, WeightAndDirection> mergedLinesToRoutingGraph(
            final Graph<Coordinate, Weight> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, Weight>> coordinateToVertex,
            final List<LineString> mergedLines) {
        logger.info("Creating routing graph from {} merged lines.", mergedLines.size());

        final var routingGraph = new Graph<Coordinate, WeightAndDirection>();
        for (final var line : mergedLines) {
            insertRoutingEdge(completeGraph, coordinateToVertex, routingGraph, line);
            insertRoutingEdge(completeGraph, coordinateToVertex, routingGraph, line.reverse());
        }
        return routingGraph;
    }

    private void removeDegreeTwoVertices(
            final Graph<Coordinate, WeightAndDirection> routingGraph,
            final Graph<Coordinate, Weight> completeGraph) {
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

    private void insertRoutingEdge(
            final Graph<Coordinate, Weight> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, Weight>> coordinateToVertex,
            final Graph<Coordinate, WeightAndDirection> routingGraph,
            final LineString line) {

        final var coordinates = line.getCoordinates();

        final var first = coordinateToVertex.get(coordinates[0]);
        final var second = coordinateToVertex.get(coordinates[1]);
        final var last = coordinateToVertex.get(coordinates[coordinates.length - 1]);

        // Special case: loop - skip for now
        if (first.equals(last)) {
            return;
        }

        routingGraph.getOrAddVertex(first.getId(), first.getProperties());
        routingGraph.getOrAddVertex(last.getId(), last.getProperties());

        final var routingEdge = routingGraph.getOrAddDirectedEdge(first.getId(), last.getId());
        final var weight = weight(line, completeGraph, coordinateToVertex);

        if (routingEdge.getProperties() == null || routingEdge.getProperties().weight() > weight) {
            routingEdge.setProperties(new WeightAndDirection(weight, second));
        }
    }

    private float weight(
            final LineString lineString,
            final Graph<Coordinate, Weight> completeGraph,
            final Map<Coordinate, Vertex<Coordinate, Weight>> coordinateToVertex) {
        var weight = 0.0f;
        for (final var segment : JtsUtil.segments(lineString)) {
            final var first = coordinateToVertex.get(segment.p0);
            final var second = coordinateToVertex.get(segment.p1);
            weight += completeGraph.getEdge(first, second).orElseThrow().getProperties().weight();
        }
        return weight;
    }

    private void mergeEdgesAndRemoveVertex(
            final Vertex<Coordinate, WeightAndDirection> vertex,
            final Graph<Coordinate, WeightAndDirection> routingGraph) {

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
                new WeightAndDirection(
                        edgeA.getProperties().weight() + edgeB.getProperties().weight(),
                        edgeA.getProperties().direction()));

        final var backwards = routingGraph.getOrAddUndirectedEdge(edgeB.getFrom(), edgeA.getFrom());
        backwards.setProperties(
                new WeightAndDirection(
                        edgeA.getProperties().weight() + edgeB.getProperties().weight(),
                        edgeB.getProperties().direction()));

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
    private Comparator<T> comparator(@Nullable final ToDoubleFunction<T> priorityExtractor) {
        if (priorityExtractor == null) {
            return Comparator.comparingInt(x -> 0);
        }
        return Comparator.comparingDouble(priorityExtractor).reversed();
    }
}
