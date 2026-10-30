package ch.ubique.uetlib.geo.spatial;

import java.util.Collection;
import java.util.function.Function;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdNodeVisitor;
import org.locationtech.jts.index.kdtree.KdTree;

public class TypedKdTree<T> {

    private final KdTree kdTree;

    public TypedKdTree() {
        kdTree = new KdTree();
    }

    public TypedKdTree(final double tolerance) {
        kdTree = new KdTree(tolerance);
    }

    public TypedKdTree(
            final Collection<T> collection, final Function<T, Coordinate> coordinateExtractor) {
        this();
        collection.forEach(item -> insert(coordinateExtractor.apply(item), item));
    }

    public int depth() {
        return kdTree.depth();
    }

    public boolean isEmpty() {
        return kdTree.isEmpty();
    }

    public int size() {
        return kdTree.size();
    }

    public KdNode insert(final Coordinate itemEnv, final T item) {
        return kdTree.insert(itemEnv, item);
    }

    public Collection<T> query(final Envelope searchEnv) {
        //noinspection unchecked
        return kdTree.query(searchEnv).stream().map(node -> ((KdNode) node).getData()).toList();
    }

    public Collection<T> query(final Coordinate coordinate, final double maxDistance) {
        final var queryEnvelope = new Envelope(coordinate);
        queryEnvelope.expandBy(maxDistance);
        return query(queryEnvelope);
    }

    public void query(final Envelope searchEnv, final KdNodeVisitor visitor) {
        kdTree.query(searchEnv, visitor);
    }

    public KdNode getRoot() {
        return kdTree.getRoot();
    }

    public T getRootValue() {
        return getData(kdTree.getRoot());
    }

    public T getData(final KdNode node) {
        //noinspection unchecked
        return (T) node.getData();
    }
}
