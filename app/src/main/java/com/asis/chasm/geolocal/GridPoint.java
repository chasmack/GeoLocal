package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Projections;
import com.asis.chasm.geolocal.PointsContract.Transforms;

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
    public GridPoint(LocalPoint pt, TransformParams params) {
        double x = (pt.getX() - params.getBaseX()) * params.getScale();
        double y = (pt.getY() - params.getBaseY()) * params.getScale();
        double rot = Math.toRadians(params.getRotate());
        this.x = x * Math.cos(rot) - y * Math.sin(rot) + params.getGridX();
        this.y = x * Math.sin(rot) + y * Math.cos(rot) + params.getGridY();
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

    public LocalPoint toLocal(TransformParams xp){
        double x = (this.x - xp.getGridX()) / xp.getScale();
        double y = (this.y - xp.getGridY()) / xp.getScale();
        double rot = -1.0 * Math.toRadians(xp.getRotate());
        return new LocalPoint(
                x * Math.cos(rot) - y * Math.sin(rot) + xp.getBaseX(),
                x * Math.sin(rot) + y * Math.cos(rot) + xp.getBaseY());
    }

    public GeoPoint toGeo(TransformParams xp) {
        switch (xp.getProjection()) {

            case Projections.PROJECTION_TM:
                return TransformTM.toGeo(this, xp);

            case Projections.PROJECTION_LC:
                return TransformLC.toGeo(this, xp);

            default:
                throw new InvalidParameterException("Bad projection.");
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
