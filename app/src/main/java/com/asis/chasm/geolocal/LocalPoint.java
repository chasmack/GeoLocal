package com.asis.chasm.geolocal;

/**
 * Local point with x/y coordinates.
 */
public class LocalPoint {

    /*
    * Units for the coordinates.
    */
    private int units;

    /*
    * x/y (easting/northing) coordinates in user units
    */
    private double x;
    private double y;

    public LocalPoint() { }
    public LocalPoint(double x, double y, int units) {
        this.x = x;
        this.y = y;
        this.units = units;
    }
    public LocalPoint(GridPoint pt, TransformParams params) {
        // convert grid coordinates to local
        double x = (pt.getX() - params.getGridX()) / params.getScale();
        double y = (pt.getY() - params.getGridY()) / params.getScale();
        double rot = -1.0 * Math.toRadians(params.getRotate());
        this.x = x * Math.cos(rot) - y * Math.sin(rot) + params.getBaseX();
        this.y = x * Math.sin(rot) + y * Math.cos(rot) + params.getBaseY();
        this.units = params.getUnits();
    }

    public LocalPoint setX(double x) {
        this.x = x;
        return this;
    }
    public LocalPoint setY(double y) {
        this.y = y;
        return this;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GridPoint
        LocalPoint pt = (LocalPoint) o;
        if (this.x == pt.x && this.y == pt.y) { return true; }
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
        return "local x/y: " + x + ", " + y;
    }
}
