package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.core.utils.CollectionUtils;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class LinesToRoutingGraphUtil<T> {

    private static final double DEFAULT_SNAP_TOLERANCE = 1;
    private static final double DEFAULT_VERTICAL_SPLIT_THRESHOLD = 50;

    private final Collection<LineString> lines;
    private final WeightingFunction<T> weightingFunction;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private double snapTolerance = DEFAULT_SNAP_TOLERANCE;
    private double verticalSplitThreshold = DEFAULT_VERTICAL_SPLIT_THRESHOLD;
    private ToDoubleFunction<T> snappingPriority;
    private ToDoubleFunction<T> edgePriority;
    private Predicate<T> isHighway;

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

    public LinesToRoutingGraphUtil<T> verticalSplitThreshold(final double verticalSplitThreshold) {
        this.verticalSplitThreshold = verticalSplitThreshold;
        return this;
    }

    /**
     * Set a predicate that determines whether vertices of the given line should be considered
     * highway vertices.
     */
    public LinesToRoutingGraphUtil<T> isHighway(final Predicate<T> isHighway) {
        this.isHighway = isHighway;
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
        final var completeGraph = new Graph<RoutingGraphVertexProperties, Weight>();
        final var coordinateToVertex = createVertices(completeGraph, nodedSegmentStrings);
        createEdges(completeGraph, nodedSegmentStrings, coordinateToVertex);
        splitUpVerticesWithDifferentElevation(completeGraph);
        logger.info("Created complete graph {}.", completeGraph);

        logger.info("Creating routing graph.");
        final var routingGraph = RoutingGraphUtil.createRoutingGraph(completeGraph);

        logger.info("Created routing graph {}.", routingGraph);

        return new RoutingGraphs(completeGraph, routingGraph);
    }

    private void createEdges(
            final Graph<RoutingGraphVertexProperties, Weight> graph,
            final List<LineString> lineStrings,
            final Map<Coordinate, Vertex<RoutingGraphVertexProperties, Weight>>
                    coordinateToVertex) {
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

    private Map<Coordinate, Vertex<RoutingGraphVertexProperties, Weight>> createVertices(
            final Graph<RoutingGraphVertexProperties, Weight> graph,
            final List<LineString> nodedSegmentStrings) {
        logger.info(
                "Getting distinct coordinates from {} noded segment strings",
                nodedSegmentStrings.size());

        final var coordinates =
                nodedSegmentStrings.stream()
                        .map(Geometry::getCoordinates)
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toUnmodifiableSet());

        final var highwayCoordinates =
                nodedSegmentStrings.stream()
                        .filter(this::isHighway)
                        .map(Geometry::getCoordinates)
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toUnmodifiableSet());

        logger.info("Creating vertices for {} coordinates", coordinates.size());
        final var lastId = new AtomicInteger();
        return coordinates.stream()
                .collect(
                        Collectors.toMap(
                                coordinate -> coordinate,
                                coordinate ->
                                        graph.addVertex(
                                                lastId.incrementAndGet(),
                                                new RoutingGraphVertexProperties(
                                                        coordinate,
                                                        highwayCoordinates.contains(coordinate)))));
    }

    private boolean isHighway(final LineString s) {
        if (isHighway == null) {
            return false;
        }
        return isHighway.test(userData(s));
    }

    private void splitUpVerticesWithDifferentElevation(
            final Graph<RoutingGraphVertexProperties, Weight> completeGraph) {

        final var candidates = findCandidatesToSplitUp(completeGraph);
        logger.info("Splitting up the following vertices for different elevations: {}", candidates);
        for (final var vertex : candidates) {
            splitUpVertex(completeGraph, vertex);
        }
    }

    private void splitUpVertex(
            final Graph<RoutingGraphVertexProperties, Weight> completeGraph,
            final Vertex<RoutingGraphVertexProperties, Weight> vertex) {

        logger.debug("Processing candidate {} {}", vertex, vertex.getProperties().coordinate());

        final var heightClasses = groupNeighborsByElevation(vertex);

        // Create new replacement vertex for each height class
        for (final var heightClass : heightClasses.entrySet()) {
            createNewVertexForHeightClass(completeGraph, vertex, heightClass);
        }

        completeGraph.removeVertex(vertex);
    }

    private void createNewVertexForHeightClass(
            final Graph<RoutingGraphVertexProperties, Weight> completeGraph,
            final Vertex<RoutingGraphVertexProperties, Weight> vertex,
            final Map.Entry<Double, Collection<Vertex<RoutingGraphVertexProperties, Weight>>>
                    heightClass) {

        logger.debug("Processing height class {}", heightClass);
        final var height = heightClass.getKey();
        final var neighbors = heightClass.getValue();

        final var properties = vertex.getProperties().copy();
        properties.coordinate().setZ(height);
        final var newVertex = completeGraph.createVertex(properties);
        logger.debug("Created vertex {}", newVertex);

        for (final var neighbor : neighbors) {
            logger.debug(
                    "Processing neighbor {} with elevation {}",
                    neighbor,
                    neighbor.getProperties().coordinate().getZ());
            updateEdgesToNewVertex(completeGraph, vertex, newVertex, neighbor);
        }
    }

    private static void updateEdgesToNewVertex(
            final Graph<RoutingGraphVertexProperties, Weight> completeGraph,
            final Vertex<RoutingGraphVertexProperties, Weight> vertex,
            final Vertex<RoutingGraphVertexProperties, Weight> newVertex,
            final Vertex<RoutingGraphVertexProperties, Weight> neighbor) {

        final var edge = vertex.getEdgeTo(neighbor).orElseThrow();
        final var twin = edge.getTwin().orElseThrow();

        completeGraph.addDirectedEdge(newVertex, neighbor, edge.getProperties());
        completeGraph.addDirectedEdge(neighbor, newVertex, twin.getProperties());
        completeGraph.removeEdge(edge);
        completeGraph.removeEdge(twin);
    }

    private Collection<Vertex<RoutingGraphVertexProperties, Weight>> findCandidatesToSplitUp(
            final Graph<RoutingGraphVertexProperties, Weight> completeGraph) {

        return completeGraph.getVertices().stream()
                .filter(vertex -> vertex.degree() > 2)
                .filter(vertex -> groupNeighborsByElevation(vertex).size() > 1)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Map<Double, Collection<Vertex<RoutingGraphVertexProperties, Weight>>>
            groupNeighborsByElevation(final Vertex<RoutingGraphVertexProperties, Weight> vertex) {
        return CollectionUtils.groupByValue(
                vertex.getNeighbors(),
                v ->
                        Double.isNaN(v.getProperties().coordinate().getZ())
                                ? 0
                                : v.getProperties().coordinate().getZ(),
                verticalSplitThreshold);
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
