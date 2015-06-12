package com.asis.chasm.geolocal;

/**
 * Local point with x/y coordinates.
 */
public class LocalPoint {

    /*
    * x/y (easting/northing) coordinates in user map units
    */
    private double x;
    private double y;

    public LocalPoint() {
    }

    public LocalPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalPoint) {
            LocalPoint p = (LocalPoint) o;
            return p.x == this.x && p.y == this.y;
        }
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
        return "(" + x + ", " + y + ")";
    }
}
