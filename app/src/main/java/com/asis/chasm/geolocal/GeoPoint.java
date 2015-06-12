package com.asis.chasm.geolocal;

/**
 * GeoPoint represents a geographic point with lat/lon coordinates.
 */

public class GeoPoint {

    /*
    * Latitude and longitude in decimal degrees.
    * South latitudes and West longitudes are negative.
    */
    private double lat;
    private double lon;

    public GeoPoint() {
    }

    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void setLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GeoPoint) {
            GeoPoint p = (GeoPoint) o;
            return p.lat == this.lat && p.lon == this.lon;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        long bits = Double.doubleToLongBits(lat);
        int code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(lon);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        return hash;
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }
}
