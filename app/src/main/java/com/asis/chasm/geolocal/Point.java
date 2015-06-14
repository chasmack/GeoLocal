package com.asis.chasm.geolocal;

/**
 * Local point with x/y coordinates.
 */
public class Point {

    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_GEOGRAPHIC = 2;

    /*
    * Point type determined by the points origin.
    */
    private int type;

    /*
    * x/y (easting/northing) coordinates in user map units
    */
    private double x;
    private double y;

    /*
    * Latitude and longitude in decimal degrees.
    * South latitudes and West longitudes are negative.
    */
    private double lat;
    private double lon;


    public Point(int type) {
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            Point p = (Point) o;
            return this.type == p.type
                    && this.x == p.x && this.y == p.y
                    && this.lat == p.lat && this.lon == p.lon;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = type;

        long bits = Double.doubleToLongBits(x);
        int code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(y);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        bits = Double.doubleToLongBits(lat);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(lon);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        return hash;
    }

    @Override
    public String toString() {
        if (type == TYPE_LOCAL) {
            return "y/x: " + y + ", " + x + "(lat/lon: " + lat + ", " + lon + ")";
        } else {
            return "lat/lon: " + lat + ", " + lon + "(y/x: " + y + ", " + x + ")";
        }
    }
}
