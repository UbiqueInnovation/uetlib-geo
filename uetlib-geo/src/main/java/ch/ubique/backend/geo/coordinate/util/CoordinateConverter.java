package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;

public class CoordinateConverter {

    private CoordinateConverter() {}

    // Convert to LV95 Coordinate

    public static LV95Coordinate toLV95(LV03Coordinate lv03) {
        double east = lv03.getEast() + 2e6;
        double north = lv03.getNorth() + 1e6;
        return new LV95Coordinate(east, north, lv03.getElevation());
    }

    public static LV95Coordinate toLV95(WGS84Coordinate wgs84) {
        double north = WGStoCHx(wgs84.getLatitude(), wgs84.getLongitude()) + 1e6;
        double east = WGStoCHy(wgs84.getLatitude(), wgs84.getLongitude()) + 2e6;
        Double elevation = null;
        if (wgs84.getElevation() != null) {
            elevation = WGStoCHh(wgs84.getLatitude(), wgs84.getLongitude(), wgs84.getElevation());
        }
        return new LV95Coordinate(east, north, elevation);
    }

    public static LV95Coordinate toLV95(WebMercatorCoordinate webMercatorCoordinate) {
        return toLV95(toWGS84(webMercatorCoordinate));
    }

    // Convert to LV03 Coordinate

    public static LV03Coordinate toLV03(LV95Coordinate lv95) {
        double east = lv95.getEast() - 2e6;
        double north = lv95.getNorth() - 1e6;
        return new LV03Coordinate(east, north, lv95.getElevation());
    }

    public static LV03Coordinate toLV03(WGS84Coordinate wgs84) {
        return toLV03(toLV95(wgs84));
    }

    public static LV03Coordinate toLV03(WebMercatorCoordinate webMercatorCoordinate) {
        return toLV03(toWGS84(webMercatorCoordinate));
    }

    // Convert to WGS84 Coordinate

    public static WGS84Coordinate toWGS84(LV95Coordinate lv95) {
        double east = lv95.getEast() - 2e6;
        double north = lv95.getNorth() - 1e6;
        Double elevation = lv95.getElevation();

        double latitude = chToWGSlat(east, north);
        double longitude = chToWGSlng(east, north);
        if (elevation != null) {
            elevation = chToWGSheight(east, north, elevation);
        }
        return new WGS84Coordinate(latitude, longitude, elevation);
    }

    public static WGS84Coordinate toWGS84(LV03Coordinate lv03) {
        return toWGS84(toLV95(lv03));
    }

    public static WGS84Coordinate toWGS84(WebMercatorCoordinate webMercatorCoordinate) {
        // The following code was adapted from
        // https://github.com/openmobilemaps/maps-core/blob/dfabe5f5573cad4078beac73c93ebb0eb4b2152f/shared/src/map/coordinates/EPSG3857ToEPSG4326Converter.h
        // Appreciation for Christoph Maurhofer (https://github.com/maurhofer-ubique) to helping me
        // on a sunny winter day.
        double lon = webMercatorCoordinate.getX() * 180 / 20037508.34;
        double lat =
                Math.atan(Math.exp(webMercatorCoordinate.getY() * Math.PI / 20037508.34))
                                * 360
                                / Math.PI
                        - 90;
        return new WGS84Coordinate(lat, lon);
    }

    // WARNING!
    // The following code consistently uses x for north and y for east
    // The following code was copied:

    // The MIT License (MIT)
    //
    // Copyright (c) 2014 Federal Office of Topography swisstopo, Wabern, CH
    //
    // Permission is hereby granted, free of charge, to any person obtaining a copy
    //  of this software and associated documentation files (the "Software"), to deal
    //  in the Software without restriction, including without limitation the rights
    //  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    //  copies of the Software, and to permit persons to whom the Software is
    //  furnished to do so, subject to the following conditions:
    //
    // The above copyright notice and this permission notice shall be included in
    //  all copies or substantial portions of the Software.
    //
    // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    //  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    //  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    //  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    //  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    //  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    //  THE SOFTWARE.
    //

    // Source:
    // http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/sys/refsys/projections.html (see PDFs under "Documentation")
    // Updated 9 dec 2014
    // Please validate your results with NAVREF on-line service:
    // http://www.swisstopo.admin.ch/internet/swisstopo/en/home/apps/calc/navref.html (difference ~
    // 1-2m)

    // Convert CH y/x/h to WGS height
    private static double chToWGSheight(double y, double x, double h) {
        // Converts military to civil and to unit = 1000km
        // Auxiliary values (% Bern)
        double yAux = (y - 600000) / 1000000;
        double xAux = (x - 200000) / 1000000;

        // Process height
        return (h + 49.55) - (12.60 * yAux) - (22.64 * xAux);
    }

    // Convert CH y/x to WGS lat
    private static double chToWGSlat(double y, double x) {
        // Converts military to civil and to unit = 1000km
        // Auxiliary values (% Bern)
        double yAux = (y - 600000) / 1000000;
        double xAux = (x - 200000) / 1000000;

        // Process lat
        double lat =
                (16.9023892 + (3.238272 * xAux))
                        - (0.270978 * Math.pow(yAux, 2))
                        - (0.002528 * Math.pow(xAux, 2))
                        - (0.0447 * Math.pow(yAux, 2) * xAux)
                        - (0.0140 * Math.pow(xAux, 3));

        // Unit 10000" to 1 " and converts seconds to degrees (dec)
        return (lat * 100) / 36;
    }

    // Convert CH y/x to WGS long
    private static double chToWGSlng(double y, double x) {
        // Converts military to civil and to unit = 1000km
        // Auxiliary values (% Bern)
        double yAux = (y - 600000) / 1000000;
        double xAux = (x - 200000) / 1000000;

        // Process long
        double lng =
                (2.6779094
                                + (4.728982 * yAux)
                                + (0.791484 * yAux * xAux)
                                + (0.1306 * yAux * Math.pow(xAux, 2)))
                        - (0.0436 * Math.pow(yAux, 3));

        // Unit 10000" to 1" and converts seconds to degrees (dec)
        return (lng * 100) / 36;
    }

    // Convert decimal angle (degrees) to sexagesimal angle (seconds)
    private static double DecToSexAngle(double dec) {
        int deg = (int) Math.floor(dec);
        int min = (int) Math.floor((dec - deg) * 60);
        double sec = (((dec - deg) * 60) - min) * 60;

        return sec + min * 60.0 + deg * 3600.0;
    }

    /**
     * Convert LV03 to WGS84 Return a array of double that contain lat, long, and height
     *
     * @param east
     * @param north
     * @param height
     * @return
     */
    private static double[] swissToInternational(double east, double north, double height) {

        double[] d = new double[3];
        d[0] = chToWGSlat(east, north);
        d[1] = chToWGSlng(east, north);
        d[2] = chToWGSheight(east, north, height);
        return d;
    }

    /**
     * Convert WGS84 to LV03 Return an array of double that contain east, north, and height
     *
     * @param latitude
     * @param longitude
     * @param ellHeight
     * @return
     */
    private static double[] internationalToSwiss(
            double latitude, double longitude, double ellHeight) {
        // , ref double east, ref double north, ref double height
        double[] d = new double[3];

        d[0] = 2e6 + WGStoCHy(latitude, longitude);
        d[1] = 1e6 + WGStoCHx(latitude, longitude);
        d[2] = WGStoCHh(latitude, longitude, ellHeight);
        return d;
    }

    // Convert WGS lat/long (° dec) and height to CH h
    private static double WGStoCHh(double lat, double lng, double h) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double latAux = (lat - 169028.66) / 10000;
        double lngAux = (lng - 26782.5) / 10000;

        // Process h
        return (h - 49.55) + (2.73 * lngAux) + (6.94 * latAux);
    }

    // Convert WGS lat/long (° dec) to CH x
    private static double WGStoCHx(double lat, double lng) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double latAux = (lat - 169028.66) / 10000;
        double lngAux = (lng - 26782.5) / 10000;

        // Process X
        return ((200147.07
                                + (308807.95 * latAux)
                                + (3745.25 * Math.pow(lngAux, 2))
                                + (76.63 * Math.pow(latAux, 2)))
                        - (194.56 * Math.pow(lngAux, 2) * latAux))
                + (119.79 * Math.pow(latAux, 3));
    }

    // Convert WGS lat/long (° dec) to CH y
    private static double WGStoCHy(double lat, double lng) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double latAux = (lat - 169028.66) / 10000;
        double lngAux = (lng - 26782.5) / 10000;

        // Process Y
        return (600072.37 + (211455.93 * lngAux))
                - (10938.51 * lngAux * latAux)
                - (0.36 * lngAux * Math.pow(latAux, 2))
                - (44.54 * Math.pow(lngAux, 3));
    }

    private static double[] swiss1903plusToInternational(double x, double y, double z) {
        return swissToInternational(x - 2000000, y - 1000000, z);
    }
}
