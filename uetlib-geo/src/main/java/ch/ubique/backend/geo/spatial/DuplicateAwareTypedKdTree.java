package ch.ubique.backend.geo.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DuplicateAwareTypedKdTree<T> {

    private final TypedKdTree<Integer> kdTree;
    private final List<Collection<T>> items;
    private final AtomicInteger size;

    public DuplicateAwareTypedKdTree() {
        this.kdTree = new TypedKdTree<>();
        this.items = new ArrayList<>();
        this.size = new AtomicInteger(0);
    }

    public boolean isEmpty() {
        return kdTree.isEmpty();
    }

    public int size() {
        return size.get();
    }

    public void insert(final Coordinate itemEnv, final T item) {
        final var node = kdTree.insert(itemEnv, items.size());
        final var index = kdTree.getData(node);
        if (index == items.size()) {
            items.add(new LinkedList<>());
        }
        items.get(index).add(item);
        size.incrementAndGet();
    }

    public Collection<T> query(final Envelope searchEnv) {
        return kdTree.query(searchEnv).stream()
                .map(items::get)
                .flatMap(Collection::stream)
                .toList();
    }
}
