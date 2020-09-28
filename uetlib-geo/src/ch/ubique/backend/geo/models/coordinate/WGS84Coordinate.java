package ch.ubique.backend.geo.models.coordinate;

/**
 * World Geodetic System 1984 (also known as WGS84, WGS 1984, EPSG:4326)
 * Most common coordinate System
 * Also referred to as Lat/Lon-Coordinate
 */
public class WGS84Coordinate implements Coordinate {

	private double latitude;

	private double longitude;

	public WGS84Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public WGS84Coordinate setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public WGS84Coordinate setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}

	@Override
	public String toString() {
		return "WGS84(lat=" + latitude + ", lon=" + longitude + ')';
	}
}
