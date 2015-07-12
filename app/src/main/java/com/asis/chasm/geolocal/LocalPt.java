package com.asis.chasm.geolocal;

/**
 * Local point with x/y coordinates.
 */
public class LocalPt {

    private static final String TAG = "LocalPt";

    /*
    * x/y (easting/northing) coordinates in meters
    */
    private double x;
    private double y;

    public LocalPt(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public GeoPt toGeo() {
        TransformSettings s = TransformSettings.getSettings();

        // Subtract off the local reference.
        double x = this.x - s.getRefX();
        double y = this.y - s.getRefY();

        // Get grid coordinates for the geographic reference point.
        GridPt gridRef = s.getGridRef();

        // Ground to grid rotation is sum of the theta at the
        // grid reference point and the ground-to-true rotation setting.
        double rot = Math.toRadians(gridRef.getTheta() + s.getRotation());

        // Rotate, add in the grid reference and convert to geographic.
        return new GridPt(
                x * Math.cos(rot) - y * Math.sin(rot) + gridRef.getX(),
                x * Math.sin(rot) + y * Math.cos(rot) + gridRef.getY()).toGeo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GridPt
        LocalPt p = (LocalPt) o;
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
