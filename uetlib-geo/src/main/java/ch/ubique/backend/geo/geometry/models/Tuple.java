package ch.ubique.backend.geo.geometry.models;

/**
 * @deprecated since 0.5.0 use org.apache.commons.lang3.tuple.Pair
 * @param <T1>
 * @param <T2>
 */
@Deprecated(since = "0.5.0")
public class Tuple<T1, T2> {

    public T1 first;
    public T2 second;

    public Tuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
}
