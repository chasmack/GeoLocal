package com.asis.chasm.geolocal;

/**
 * Created by Charlie on 6/11/2015.
 */
public class LocalPoint {
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
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
