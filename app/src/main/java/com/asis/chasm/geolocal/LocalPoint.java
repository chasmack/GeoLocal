package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.Settings.Params;

/**
 * Local point with x/y coordinates.
 */
public class LocalPoint {

    private static final String TAG = "LocalPoint";

    /*
    * x/y (easting/northing) coordinates in meters
    */
    private double x;
    private double y;

    public LocalPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public GeoPoint toGeo() {
        Params p = Params.getParams();

        // Subtract off the local reference.
        double x = this.x - p.getRefX();
        double y = this.y - p.getRefY();

        // Get grid coordinates for the geographic reference point.
        GridPoint gridRef = p.getGridRef();

        // Ground to grid rotation is sum of the theta at the
        // grid reference point and the ground-to-true rotation setting.
        double rot = Math.toRadians(gridRef.getTheta() + p.getRotation());

        // Rotate, add in the grid reference and convert to geographic.
        return new GridPoint(
                x * Math.cos(rot) - y * Math.sin(rot) + gridRef.getX(),
                x * Math.sin(rot) + y * Math.cos(rot) + gridRef.getY()).toGeo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GridPoint
        LocalPoint p = (LocalPoint) o;
        if (this.x == p.x && this.y == p.y) { return true; }
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

        return hash;
    }

    @Override
    public String toString() {
        return "local x, y: " + x + ", " + y;
    }
}
