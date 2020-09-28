package ch.ubique.backend.geo.models.coordinate;

/**
 * Swiss coordinates (1903)
 * Imprecise, use LV95 instead if possible
 */
public class LV03Coordinate implements Coordinate {

	private double east;

	private double north;


	public LV03Coordinate(double east, double north) {
		this.east = east;
		this.north = north;
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

	@Override
	public String toString() {
		return "LV03(east=" + east + ", north=" + north + ')';
	}
}
