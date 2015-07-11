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

    public LocalPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public GeoPoint toGeo() {
        TransformSettings settings = TransformSettings.getSettings();
        double x = (this.x - settings.getRefX());
        double y = (this.y - settings.getRefY());

        // Convert the geographic reference point to grid coordinates.
        GridPoint gridRef = new GeoPoint(settings.getRefLat(), settings.getRefLon()).toGrid();

        // Grid to ground rotation is sum of the theta at the
        // grid reference point and the ground to true roataion setting.
        double rot = Math.toRadians(settings.getRotation() + gridRef.getTheta());
        return new GridPoint(
                x * Math.cos(rot) - y * Math.sin(rot) + gridRef.getX(),
                x * Math.sin(rot) + y * Math.cos(rot) + gridRef.getY()).toGeo();
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
