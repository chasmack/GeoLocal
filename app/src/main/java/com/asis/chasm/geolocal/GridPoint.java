package com.asis.chasm.geolocal;

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
    * grid scale factor (k) and theta
    */
    private double k;
    private double theta;

    public GridPoint() { }
    public GridPoint(double x, double y) {
        this.x = x;
        this.y = y;
        this.k = this.theta = 0.0;
    }
    public GridPoint(LocalPoint pt, TransformParams params) {
        // convert local coordinates to grid
        double x = (pt.getX() - params.getBaseX()) * params.getScale();
        double y = (pt.getY() - params.getBaseY()) * params.getScale();
        double rot = Math.toRadians(params.getRotate());
        this.x = x * Math.cos(rot) - y * Math.sin(rot) + params.getGridX();
        this.y = x * Math.sin(rot) + y * Math.cos(rot) + params.getGridY();
        this.k = this.theta = 0.0;
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
        return "grid x/y/k/theta: " + x + ", " + y + ", " + k + ", " + theta;
    }
}
