package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Transforms;

/**
 * Grid point with x/y coordinates.
 */
public class GridPoint {

    /*
    * Units for the coordinates.
    */
    private int units;

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

    public GridPoint(double x, double y, int units) {
        this.x = x;
        this.y = y;
        this.units = units;
        this.k = this.theta = 0.0;
    }

    public GridPoint(GridPoint p) {
        this.x = p.getX();
        this.y = p.getY();
        this.units = p.getUnits();
        this.k = p.getK();
        this.theta = p.getTheta();
    }

    /*
    * Transform a local point using the local to grid transform parameters
    */
    public GridPoint(LocalPoint pt, TransformParams params) {
        // convert local coordinates to grid in meters
        double x = (pt.getX() - params.getBaseX()) * params.getScale();
        double y = (pt.getY() - params.getBaseY()) * params.getScale();
        double rot = Math.toRadians(params.getRotate());
        this.x = x * Math.cos(rot) - y * Math.sin(rot) + params.getGridX();
        this.y = x * Math.sin(rot) + y * Math.cos(rot) + params.getGridY();
        this.k = this.theta = 0.0;
        this.units = params.getUnits();
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
    public int getUnits() {
        return units;
    }

    public GeoPoint toGeo(TransformParams xp) {
        return TransformLC.toGeo(this, xp);
    }

    public GridPoint toMeters() {
        switch (units) {
            case Transforms.UNITS_SURVEY_FT:
                x /= Transforms.SURVEY_FT_PER_METER;
                y /= Transforms.SURVEY_FT_PER_METER;
                break;
            case Transforms.UNITS_INTERNATIONAL_FT:
                x /= Transforms.INTERNATIONAL_FT_PER_METER;
                y /= Transforms.INTERNATIONAL_FT_PER_METER;
                break;
        }
        units = Transforms.UNITS_METERS;
        return this;
    }

    public GridPoint toSurveyFeet() {
        switch (units) {
            case Transforms.UNITS_METERS:
                x *= Transforms.SURVEY_FT_PER_METER;
                y *= Transforms.SURVEY_FT_PER_METER;
                break;
            case Transforms.UNITS_INTERNATIONAL_FT:
                x *= (Transforms.UNITS_SURVEY_FT / Transforms.INTERNATIONAL_FT_PER_METER);
                y *= (Transforms.UNITS_SURVEY_FT / Transforms.INTERNATIONAL_FT_PER_METER);
                break;
        }
        units = Transforms.UNITS_SURVEY_FT;
        return this;
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
