package com.asis.chasm.geolocal;

import android.util.Log;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;

/**
 * Local point with x/y coordinates.
 */
public class Point {

    // Use for logging and debugging
    private static final String TAG = "Point";

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

    public Point setXY(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Point setLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    public Point toGeographic(TransformParams params)  {
        Log.d(TAG, "toGeographic x/y: " + x + "/" + y);
        switch (params.getProjection()) {
            case Projections.PROJECTION_LC:
                TransformLambertConic.toGeographic(this, params);
                break;
            case Projections.PROJECTION_TM:
                TransformTransverseMercator.toGeographic(this, params);
                break;
            default:
                setLatLon(0.0, 0.0);
                break;
        }
        return this;
    }

    public Point toLocal(TransformParams params) {
        Log.d(TAG, "toLocal lat/lon: " + lat + "/" + lon);
        switch (params.getProjection()) {
            case Projections.PROJECTION_LC:
                TransformLambertConic.toLocal(this, params);
                break;
            case Projections.PROJECTION_TM:
                TransformTransverseMercator.toLocal(this, params);
                break;
            default:
                setXY(0.0, 0.0);
                break;
        }
        return this;
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
        if (type == Points.POINT_TYPE_LOCAL) {
            return "y/x: " + y + ", " + x + "(lat/lon: " + lat + ", " + lon + ")";
        } else {
            return "lat/lon: " + lat + ", " + lon + "(y/x: " + y + ", " + x + ")";
        }
    }
}
