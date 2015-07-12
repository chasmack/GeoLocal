package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Projections;

/**
 * Geographic point with lat/lon coordinates.
 */
public class GeoPt {

    /*
    * Latitude and longitude in decimal degrees.
    * South latitudes and West longitudes are negative.
    */
    private double lat;
    private double lon;

    public GeoPt(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }

    public LocalPt toLocal() {
        TransformSettings s = TransformSettings.getSettings();

        // Get grid coordinates for the geographic reference point.
        GridPt gridRef = s.getGridRef();

        // Convert coordinates to grid and subtract off the grid reference.
        GridPt grid = this.toGrid();
        double x = grid.getX() - gridRef.getX();
        double y = grid.getY() - gridRef.getY();

        // Grid to ground rotation -1.0 times the sum of theta at the
        // grid reference point and the ground-to-true roataion setting.
        double rot = -1.0 * Math.toRadians(gridRef.getTheta() + s.getRotation());

        // Rotate, add in the local reference and return local point.
        return new LocalPt(
                x * Math.cos(rot) - y * Math.sin(rot) + s.getRefX(),
                x * Math.sin(rot) + y * Math.cos(rot) + s.getRefY());
    }

    public GridPt toGrid() {
        TransformSettings s = TransformSettings.getSettings();
        switch (s.getProjection()) {

            case Projections.PROJECTION_LC:
                return TransformLC.toGrid(this);
            case Projections.PROJECTION_TM:
                return TransformTM.toGrid(this);
            default:
                throw new IllegalArgumentException("Bad projection: " + s.getProjection());
        }
    }

    public double getTheta() {
        TransformSettings s = TransformSettings.getSettings();
        switch (s.getProjection()) {

            case Projections.PROJECTION_LC:
                return TransformLC.getTheta(this);
            case Projections.PROJECTION_TM:
                return TransformTM.getTheta(this);
            default:
                throw new IllegalArgumentException("Bad projection: " + s.getProjection());
        }
    }

    public double getK() {
        TransformSettings s = TransformSettings.getSettings();
        switch (s.getProjection()) {

            case Projections.PROJECTION_LC:
                return TransformLC.getK(this);
            case Projections.PROJECTION_TM:
                return TransformTM.getK(this);
            default:
                throw new IllegalArgumentException("Bad projection: " + s.getProjection());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of GeoPt
        GeoPt p = (GeoPt) o;
        if (this.lat == p.lat && this.lon == p.lon) { return true; }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        long bits = Double.doubleToLongBits(lat);
        int code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;
        bits = Double.doubleToLongBits(lon);
        code = (int)(bits ^ (bits >> 32));
        hash = hash * 31 + code;

        return hash;
    }

    @Override
    public String toString() {
        return "geographic lat, lon: " + lat + ", " + lon;
    }
}
