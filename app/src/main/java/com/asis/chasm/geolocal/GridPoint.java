package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Projections;

import java.security.InvalidParameterException;

/**
 * Grid point with x/y coordinates.
 */
public class GridPoint {

    /*
    * x/y (easting/northing) coordinates in meters
    */
    private double x;
    private double y;

    /*
    * grid scale factor (k) and theta (degrees)
    */
    private double k;
    private double theta;

    public GridPoint(double x, double y) {
        this.x = x;
        this.y = y;
        this.k = 1.0;
        this.theta = 0.0;
    }

    /*
    * Transform a local coordinates to grid using the transform parameters
    */
    public GridPoint(LocalPoint pt, TransformSettings settings) {
        double x = (pt.getX() - settings.getBaseX()) * settings.getScale();
        double y = (pt.getY() - settings.getBaseY()) * settings.getScale();
        double rot = Math.toRadians(settings.getRotate());
        this.x = x * Math.cos(rot) - y * Math.sin(rot) + settings.getGridX();
        this.y = x * Math.sin(rot) + y * Math.cos(rot) + settings.getGridY();
        this.k = 1.0;
        this.theta = 0.0;
    }

    public GridPoint setX(double x) {
        this.x = x;
        return this;
    }
    public GridPoint setY(double y) {
        this.y = y;
        return this;
    }
    public GridPoint setK(double k) {
        this.k = k;
        return this;
    }
    public GridPoint setTheta(double theta) {
        this.theta = theta;
        return this;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getK() {
        return k;
    }
    public double getTheta() {
        return theta;
    }

    public LocalPoint toLocal(TransformSettings settings){
        double x = (this.x - settings.getGridX()) / settings.getScale();
        double y = (this.y - settings.getGridY()) / settings.getScale();
        double rot = -1.0 * Math.toRadians(settings.getRotate());
        return new LocalPoint(
                x * Math.cos(rot) - y * Math.sin(rot) + settings.getBaseX(),
                x * Math.sin(rot) + y * Math.cos(rot) + settings.getBaseY());
    }

    public GeoPoint toGeo(TransformSettings settings) {
        switch (settings.getProjection()) {

            case Projections.PROJECTION_TM:
                return TransformTM.toGeo(this, settings);

            case Projections.PROJECTION_LC:
                return TransformLC.toGeo(this, settings);

            default:
                throw new IllegalArgumentException("Bad projection: " + settings.getProjection());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GridPoint
        GridPoint pt = (GridPoint) o;
        if (this.x == pt.x && this.y == pt.y
                && this.k == pt.k && this.theta == pt.theta) { return true; }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        long bits = Double.doubleToLongBits(x);
        int code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(y);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        bits = Double.doubleToLongBits(k);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(theta);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        return hash;
    }

    @Override
    public String toString() {
        return "grid x, y, k, theta: " + x + ", " + y + ", " + k + ", " + theta;
    }
}
