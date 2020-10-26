package ch.ubique.backend.geo.coordinate.models;

public interface Coordinate <C extends Coordinate<C>> {

    double distanceTo(C other);

    double flatDistanceTo(C other);
}
