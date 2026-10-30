package ch.ubique.uetlib.geo.graph.routing.utils;

import ch.ubique.uetlib.geo.coordinate.util.JtsUtil;
import java.util.ArrayList;
import java.util.Collection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.kdtree.KdTree;

public class Snapping3dPointIndex {

    private final KdTree kdTree;
    private final double snapTolerance;

    /**
     * @param snapTolerance points are snapped if within this distance
     */
    public Snapping3dPointIndex(final double snapTolerance) {
        kdTree = new KdTree(snapTolerance);
        this.snapTolerance = snapTolerance;
    }

    /**
     * Snaps a coordinate to an existing snap point, if it is within the snap tolerance distance.
     * Otherwise, adds the coordinate to the snap point index.
     *
     * @param coordinate the point to snap
     * @return the point it snapped to, or the input point
     */
    public Coordinate snap(final Coordinate coordinate) {
        final var node = kdTree.insert(coordinate, new ArrayList<Coordinate>(1));

        @SuppressWarnings("unchecked")
        final var coordinatesAtPlanarLocation = (Collection<Coordinate>) node.getData();

        return coordinatesAtPlanarLocation.stream()
                .filter(c -> JtsUtil.zDistance(coordinate, c) <= snapTolerance)
                .findAny()
                .orElseGet(
                        () -> {
                            coordinatesAtPlanarLocation.add(coordinate);
                            return coordinate;
                        });
    }
}
