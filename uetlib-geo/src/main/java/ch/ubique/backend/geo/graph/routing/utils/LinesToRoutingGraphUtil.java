package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Graph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class LinesToRoutingGraphUtil {

    private static final double DEFAULT_SNAP_TOLERANCE = 1;

    private LinesToRoutingGraphUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(LinesToRoutingGraphUtil.class);

    public static <T> Graph<Coordinate, T> buildGraph(final Collection<LineString> lines, final double snapTolerance) {

        final var graph = new Graph<Coordinate, T>();
        if (lines.isEmpty()) {
            logger.info("No paths provided, returning empty graph.");
            return graph;
        }

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
        final var nodedSegmentStrings = (List<NodedSegmentString>) noder.getNodedSubstrings();

        logger.info("Getting distinct coordinates from {} noded segment strings", nodedSegmentStrings.size());
        final var coordinates = nodedSegmentStrings.stream()
                .map(NodedSegmentString::getCoordinates).flatMap(Arrays::stream)
                .distinct()
                .toList();

        logger.info("Creating vertices for {} coordinates", coordinates.size());
        final var lastId = new AtomicInteger();
        final var coordinateToVertex = coordinates.stream().collect(Collectors.toMap(
                coordinate -> coordinate,
                coordinate -> graph.addVertex(lastId.incrementAndGet(), coordinate)));


        logger.info("Creating edges by linking neighbors of each path");
        for (final var segmentString : nodedSegmentStrings) {
            final var segmentCoordinates = segmentString.getCoordinates();
            //noinspection unchecked
            final var info = (T)((Geometry) segmentString.getData()).getUserData();
            for (int i = 0; i < segmentCoordinates.length - 1; i++) {
                final var from = coordinateToVertex.get(segmentCoordinates[i]);
                final var to = coordinateToVertex.get(segmentCoordinates[i + 1]);
                if (from != to) {
                    graph.getOrAddEdge(from, to, info);
                }
            }
        }

        return graph;
    }

    public static <T> Graph<Coordinate, T> buildGraphWithInfo(final Collection<LineWithInfo<T>> linesWithInfo, final double snapTolerance) {
        return buildGraph(linesWithInfo.stream().map(lineWithInfo -> {
            lineWithInfo.line().setUserData(lineWithInfo.info());
            return lineWithInfo.line();
        }).toList(), snapTolerance);
    }

    public static <T> Graph<Coordinate, T> buildGraph(final Collection<LineString> lines) {
        return buildGraph(lines, DEFAULT_SNAP_TOLERANCE);
    }

    public static <T> Graph<Coordinate, T> buildGraphWithInfo(final Collection<LineWithInfo<T>> linesWithInfo) {
        return buildGraphWithInfo(linesWithInfo, DEFAULT_SNAP_TOLERANCE);
    }
}
