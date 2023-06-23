package ch.ubique.backend.geo.spatial;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypedQuadtree<T> {

    private final Quadtree quadTree;

    public TypedQuadtree() {
        quadTree = new Quadtree();
    }

    public TypedQuadtree(
            final Collection<T> collection, final Function<T, Envelope> envelopeFunction) {
        this();
        collection.forEach(item -> insert(envelopeFunction.apply(item), item));
    }

    public TypedQuadtree(
            final Stream<? extends T> collection, final Function<T, Envelope> envelopeFunction) {
        this();
        collection.forEach(item -> insert(envelopeFunction.apply(item), item));
    }

    public int depth() {
        return quadTree.depth();
    }

    public boolean isEmpty() {
        return quadTree.isEmpty();
    }

    public int size() {
        return quadTree.size();
    }

    public void insert(final Envelope itemEnv, final T item) {
        quadTree.insert(itemEnv, item);
    }

    public boolean remove(final Envelope itemEnv, final T item) {
        return quadTree.remove(itemEnv, item);
    }

    public Collection<T> query(final Envelope searchEnv) {
        //noinspection unchecked
        return quadTree.query(searchEnv);
    }

    public void query(final Envelope searchEnv, final ItemVisitor visitor) {
        quadTree.query(searchEnv, visitor);
    }

    public Collection<T> queryAll() {
        //noinspection unchecked
        return quadTree.queryAll();
    }
}
