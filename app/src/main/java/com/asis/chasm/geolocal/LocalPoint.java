package com.asis.chasm.geolocal;

/**
 * Local point with x/y coordinates.
 */
public class LocalPoint {

    /*
    * x/y (easting/northing) coordinates in meters
    */
    private double x;
    private double y;

    public LocalPoint() { }
    public LocalPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public GridPoint toGrid(TransformSettings xp){
        double x = (this.x - xp.getBaseX()) * xp.getScale();
        double y = (this.y - xp.getBaseY()) * xp.getScale();
        double rot = Math.toRadians(xp.getRotate());
        GridPoint grid = new GridPoint(
                x * Math.cos(rot) - y * Math.sin(rot) + xp.getGridX(),
                x * Math.sin(rot) + y * Math.cos(rot) + xp.getGridY());
        return grid;
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
