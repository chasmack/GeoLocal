package com.asis.chasm.geolocal;

/**
 * Transform paramaters.
 */
public class TransformParams {

    /*
    * Transformation from the local basis and units to the selected grid basis.
    */

    public static final int UNITS_METERS = 1;
    public static final int UNITS_SURVEY_FT = 2;
    public static final int UNITS_INTERNATIONAL_FT = 3;

    // Units used for local point coordinates.
    private int units;

    // Translation from local reference point to grid reference in meters.
    private double dx, dy;

    // Rotation about reference point from local basis to grid in decimal degrees.
    // A negative value rotates right (clockwise) from local to grid.
    private double rotate;

    // Scale factor from local distances to geographic distance.
    private double scale;

    /*
    * Grid projection type and constants.
    */

    public static final int PROJECTION_TM = 1;
    public static final int PROJECTION_LC = 2;

    // Type of grid projection.
    private int projection;

    // Latitude of origin, central meridian
    private double p0, m0;

    // False easting, northing
    private double x0, y0;

    // Lambert conic first and second standard parallels
    private double p1, p2;

    // Transverse mercator central scale factor (1 - 1/E)
    private double k0;

    public TransformParams() {
    }

    public void setLocalTransform(
            int units,
            double dx, double dy,
            double rotate, double scale) {
        this.units = units;
        this.dx = dx; this.dy = dy;
        this.rotate = rotate;
        this.scale = scale;
    }

    public void setProjectionTM(
            double p0, double m0,
            double x0, double y0,
            double k0) {
        projection = PROJECTION_TM;
        this.p0 = p0; this.m0 = m0;
        this.x0 = x0; this.y0 = y0;
        this.k0 = k0;
    }

    public void setProjectionLC(
            double p0, double m0,
            double x0, double y0,
            double p1, double p2) {
        projection = PROJECTION_LC;
        this.p0 = p0; this.m0 = m0;
        this.x0 = x0; this.y0 = y0;
        this.p0 = p1; this.p0 = p2;
    }

    public double getDx(){ return dx; }
    public double getDy(){ return dy; }
    public double getRotate(){ return rotate; }
    public double getScale(){ return scale; }
    public int getProjection(){ return projection; }
    public double getP0(){ return p0; }
    public double getM0(){ return m0; }
    public double getX0(){ return x0; }
    public double getY0(){ return y0; }
    public double getP1(){ return p1; }
    public double getP2(){ return p2; }
    public double getK0(){ return k0; }
}
