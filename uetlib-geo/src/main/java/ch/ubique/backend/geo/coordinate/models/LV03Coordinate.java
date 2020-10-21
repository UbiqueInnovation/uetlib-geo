package ch.ubique.backend.geo.coordinate.models;

import java.util.Objects;

/**
 * Swiss coordinates (1903)
 * Imprecise, use LV95 instead if possible
 */
public class LV03Coordinate implements Coordinate {

	// For equality testing
	private static final double EPSILON = 0.1; // = 10 cm


	private double east;

	private double north;

	private Double elevation;

	public LV03Coordinate(double east, double north) {
		this(east, north, null);
	}

	public LV03Coordinate(double east, double north, Double elevation) {
		this.east = east;
		this.north = north;
		this.elevation = elevation;
	}

	public double getEast() {
		return east;
	}

	public LV03Coordinate setEast(double east) {
		this.east = east;
		return this;
	}

	public double getNorth() {
		return north;
	}

	public LV03Coordinate setNorth(double north) {
		this.north = north;
		return this;
	}

	public Double getElevation() {
		return elevation;
	}

	public LV03Coordinate setElevation(Double elevation) {
		this.elevation = elevation;
		return this;
	}

	@Override
	public String toString() {
		return "LV03(east=" + east + ", north=" + north + ')';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LV03Coordinate that = (LV03Coordinate) o;

		if (Math.abs(that.east - east) > EPSILON) return false;
		if (Math.abs(that.north - north) > EPSILON) return false;
		return Objects.equals(elevation, that.elevation);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(east);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(north);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
		return result;
	}
}
