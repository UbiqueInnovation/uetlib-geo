package ch.ubique.backend.geo.models.coordinate;

public class LV95CoordinateWithElevation extends LV95Coordinate {

	private double elevation;

	public LV95CoordinateWithElevation(double east, double north, double elevation) {
		super(east, north);
		this.elevation = elevation;
	}

	public double getElevation() {
		return elevation;
	}

	public LV95CoordinateWithElevation setElevation(double elevation) {
		this.elevation = elevation;
		return this;
	}

	@Override
	public String toString() {
		return "LV95(east=" + getEast() + ", north=" + getNorth() + ", elevation=" + elevation + ')';
	}
}
