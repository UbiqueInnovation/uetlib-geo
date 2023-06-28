package ch.ubique.backend.geo.spatial;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypedStrTree<T> {

    private final STRtree strTree;

    public TypedStrTree() {
        strTree = new STRtree();
    }

    public TypedStrTree(
            final Collection<T> collection, final Function<T, Envelope> envelopeFunction) {
        this();
        collection.forEach(item -> insert(envelopeFunction.apply(item), item));
    }

    public TypedStrTree(
            final Stream<? extends T> collection, final Function<T, Envelope> envelopeFunction) {
        this();
        collection.forEach(item -> insert(envelopeFunction.apply(item), item));
    }

    public int depth() {
        return strTree.depth();
    }

    public boolean isEmpty() {
        return strTree.isEmpty();
    }

    public int size() {
        return strTree.size();
    }

    public void insert(final Envelope itemEnv, final T item) {
        strTree.insert(itemEnv, item);
    }

    public boolean remove(final Envelope itemEnv, final T item) {
        return strTree.remove(itemEnv, item);
    }

    public Collection<T> query(final Envelope searchEnv) {
        //noinspection unchecked
        return strTree.query(searchEnv);
    }

    public void query(final Envelope searchEnv, final ItemVisitor visitor) {
        strTree.query(searchEnv, visitor);
    }
}
