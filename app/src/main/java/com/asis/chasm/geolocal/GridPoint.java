package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Projections;

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
        this.theta = 0.0;
        this.k = 1.0;
    }
    public GridPoint setTheta(double theta) {
        this.theta = theta;
        return this;
    }
    public GridPoint setK(double k) {
        this.k = k;
        return this;
    };

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

    public GeoPoint toGeo() {
        TransformSettings settings = TransformSettings.getSettings();
        switch (settings.getProjection()) {

            case Projections.PROJECTION_LC:
                return TransformLC.toGeo(this);

            case Projections.PROJECTION_TM:
                return TransformTM.toGeo(this);

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
