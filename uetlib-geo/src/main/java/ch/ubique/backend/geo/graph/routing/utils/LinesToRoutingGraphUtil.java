package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class LinesToRoutingGraphUtil {

    private static final double DEFAULT_SNAP_TOLERANCE = 1;

    private LinesToRoutingGraphUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(LinesToRoutingGraphUtil.class);

    public static <T> RoutingGraphs<T> buildGraph(final Collection<LineString> lines, final double snapTolerance) {

        if (lines.isEmpty()) {
            logger.info("No paths provided, returning empty graph.");
            return RoutingGraphs.empty();
        }

        logger.info("Creating complete graph.");
        final var nodedSegmentStrings = computeNodedSegmentStrings(lines, snapTolerance);
        final var completeGraph = new Graph<Coordinate, T>();
        final var coordinateToVertex= createVertices(completeGraph, nodedSegmentStrings);
        createEdges(completeGraph, nodedSegmentStrings, coordinateToVertex);
        logger.info("Created complete graph {}.", completeGraph);

        logger.info("Creating routing graph.");
        final var routingGraph = createRoutingGraph(lines, nodedSegmentStrings, completeGraph, coordinateToVertex);
        logger.info("Created routing graph {}.", routingGraph);

        return new RoutingGraphs<>(completeGraph, routingGraph);
    }

    @NotNull
    public static <T> RoutingGraphs<T> buildGraphWithInfo(final Collection<LineWithInfo<T>> linesWithInfo, final double snapTolerance) {
        return buildGraph(linesWithInfo.stream().map(lineWithInfo -> {
            lineWithInfo.line().setUserData(lineWithInfo.info());
            return lineWithInfo.line();
        }).toList(), snapTolerance);
    }

    @NotNull
    public static <T> RoutingGraphs<T> buildGraph(final Collection<LineString> lines) {
        return buildGraph(lines, DEFAULT_SNAP_TOLERANCE);
    }

    @NotNull
    public static <T> RoutingGraphs<T> buildGraphWithInfo(final Collection<LineWithInfo<T>> linesWithInfo) {
        return buildGraphWithInfo(linesWithInfo, DEFAULT_SNAP_TOLERANCE);
    }

    @NotNull
    private static List<NodedSegmentString> computeNodedSegmentStrings(final Collection<LineString> lines, final double snapTolerance) {
        logger.info("Creating segment strings for {} lines.", lines.size());
        final var segmentStrings = lines.stream()
                .map(SegmentStringUtil::extractSegmentStrings)
                .flatMap(Collection::stream)
                .toList();

        // The noder merges nodes within snapTolerance and adds additional nodes for all line intersections.
        final var noder = new SnappingNoder(snapTolerance);
        logger.info("Noding segments using {} with snapTolerance {}", noder.getClass(), snapTolerance);
        noder.computeNodes(segmentStrings);
        //noinspection unchecked
        return (List<NodedSegmentString>) noder.getNodedSubstrings();
    }

    private static <T> Map<Coordinate, Vertex<Coordinate, T>> createVertices(final Graph<Coordinate, T> graph, final List<NodedSegmentString> nodedSegmentStrings) {
        logger.info("Getting distinct coordinates from {} noded segment strings", nodedSegmentStrings.size());
        final var coordinates = nodedSegmentStrings.stream()
                .map(NodedSegmentString::getCoordinates).flatMap(Arrays::stream)
                .distinct()
                .toList();

        logger.info("Creating vertices for {} coordinates", coordinates.size());
        final var lastId = new AtomicInteger();
        return coordinates.stream().collect(Collectors.toMap(
                coordinate -> coordinate,
                coordinate -> graph.addVertex(lastId.incrementAndGet(), coordinate)));
    }

    private static <T> void createEdges(final Graph<Coordinate, T> graph, final List<NodedSegmentString> nodedSegmentStrings, final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex) {
        logger.info("Creating edges by linking neighbors of each path");
        for (final var segmentString : nodedSegmentStrings) {
            final var segmentCoordinates = segmentString.getCoordinates();
            //noinspection unchecked
            final var info = (T) ((Geometry)segmentString.getData()).getUserData();
            for (int i = 0; i < segmentCoordinates.length - 1; i++) {
                final var from = coordinateToVertex.get(segmentCoordinates[i]);
                final var to = coordinateToVertex.get(segmentCoordinates[i + 1]);
                assert(!from.equals(to));
                graph.getOrAddUndirectedEdge(from, to, info);
            }
        }
    }

    @NotNull
    private static <T> Graph<Coordinate, RoutingGraphEdgeProperties<T>> createRoutingGraph(final Collection<LineString> lines, final List<NodedSegmentString> nodedSegmentStrings, final Graph<Coordinate, T> completeGraph, final Map<Coordinate, Vertex<Coordinate, T>> coordinateToVertex) {
        logger.info("Merging segment strings.");
        final var mergedLines = mergeSegmentStrings(lines, nodedSegmentStrings);

        logger.info("Creating routing graph from {} merged lines.", mergedLines.size());
        final var routingGraph = new Graph<Coordinate, RoutingGraphEdgeProperties<T>>();
        for (final var line : mergedLines) {
            final var coordinates = line.getCoordinates();

            assert(coordinates.length >= 2);

            final var first = coordinateToVertex.get(coordinates[0]);
            final var second = coordinateToVertex.get(coordinates[1]);
            final var firstEdge = completeGraph.getEdge(first, second);
            assert(firstEdge.isPresent());

            final var last = coordinateToVertex.get(coordinates[coordinates.length - 1]);
            final var secondLast = coordinateToVertex.get(coordinates[coordinates.length - 2]);
            final var lastEdge = completeGraph.getEdge(last, secondLast);
            assert(lastEdge.isPresent());

            final var distance = line.getLength();

            routingGraph.getOrAddVertex(first.getId(), first.getProperties());
            routingGraph.getOrAddVertex(last.getId(), last.getProperties());

            routingGraph.getOrAddDirectedEdge(first.getId(), last.getId(), new RoutingGraphEdgeProperties<>(distance, second, firstEdge.get().getProperties()));
            routingGraph.getOrAddDirectedEdge(last.getId(), first.getId(), new RoutingGraphEdgeProperties<>(distance, secondLast, lastEdge.get().getProperties()));

        }
        return routingGraph;
    }

    @NotNull
    private static List<LineString> mergeSegmentStrings(final Collection<LineString> lines, final List<NodedSegmentString> nodedSegmentStrings) {
        final var lineMerger = new LineMerger();
        final var factory = JtsUtil.factoryFromGeometryCollection(lines);
        lineMerger.add(nodedSegmentStrings.stream().map(segmentString -> {
            final var line = factory.createLineString(segmentString.getCoordinates());
            line.setUserData(((Geometry) segmentString.getData()).getUserData());
            return line;
        }).toList());
        //noinspection unchecked
        return (List<LineString>) lineMerger.getMergedLineStrings();
    }
}
