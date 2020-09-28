package ch.ubique.backend.geo.models.coordinate;

public class WGS84CoordinateWithElevation extends WGS84Coordinate {

    private double elevation;

    public WGS84CoordinateWithElevation(double latitude, double longitude, double elevation) {
        super(latitude, longitude);
        this.elevation = elevation;
    }

    public double getElevation() {
        return elevation;
    }

    public WGS84CoordinateWithElevation setElevation(double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public String toString() {
        return "WGS84(lat=" + getLatitude() + ", lon=" + getLongitude() + ", elevation=" + elevation + ')';
    }

}
