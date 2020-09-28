package ch.ubique.backend.geo.models.coordinate;

public class LV03CoordinateWithElevation extends LV03Coordinate {

	private double elevation;

	public LV03CoordinateWithElevation(double east, double north, double elevation) {
		super(east, north);
		this.elevation = elevation;
	}

	public double getElevation() {
		return elevation;
	}

	public LV03CoordinateWithElevation setElevation(double elevation) {
		this.elevation = elevation;
		return this;
	}

	@Override
	public String toString() {
		return "LV03(east=" + getEast() + ", north=" + getNorth() + ", elevation=" + elevation + ')';
	}
}
