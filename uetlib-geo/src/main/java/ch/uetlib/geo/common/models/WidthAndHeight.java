package ch.uetlib.geo.common.models;

public record WidthAndHeight<T>(T width, T height) {
    public static <T> WidthAndHeight<T> of(final T width, final T height) {
        return new WidthAndHeight<>(width, height);
    }
}
