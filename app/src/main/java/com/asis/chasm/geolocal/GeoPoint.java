package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Projections;

/**
 * Geographic point with lat/lon coordinates.
 */
public class GeoPoint {

    /*
    * Latitude and longitude in decimal degrees.
    * South latitudes and West longitudes are negative.
    */
    private double lat;
    private double lon;

    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public GeoPoint setLat(double lat) {
        this.lat = lat;
        return this;
    }
    public GeoPoint setLon(double lon) {
        this.lon = lon;
        return this;
    }

    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }

    public GridPoint toGrid(TransformSettings xp) {
        switch (xp.getProjection()) {

            case Projections.PROJECTION_LC:
                return TransformLC.toGrid(this, xp);

            case Projections.PROJECTION_TM:
                return TransformTM.toGrid(this, xp);

            default:
                throw new IllegalArgumentException("Bad projection.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GeoPoint
        GeoPoint p = (GeoPoint) o;
        if (this.lat == p.lat && this.lon == p.lon) { return true; }
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
        return "geographic lat, lon: " + lat + ", " + lon;
    }
}
