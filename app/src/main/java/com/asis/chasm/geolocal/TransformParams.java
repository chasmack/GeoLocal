package com.asis.chasm.geolocal;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.asis.chasm.geolocal.PointsContract.Projections;
import com.asis.chasm.geolocal.PointsContract.Transforms;

/**
 * Transform paramaters.
 */
public class TransformParams {

    // Use for logging and debugging
    private static final String TAG = "TransformParams";

    // Units used for local point coordinates.
    private int units;

    // Local (base) and grid reference points in user units.
    private double baseX, baseY;
    private double gridX, gridY;

    // Rotation about reference point from local basis to grid in decimal degrees.
    // A negative value rotates right (clockwise) from local to grid.
    private double rotate;

    // Scale factor from local distances to geographic distance.
    private double scale;

    // Type of grid projection, e.g. TM, LC, OM.
    private int projection;

    // Latitude of origin, central meridian in decimal degrees.
    private double p0, m0;

    // False easting, northing in meters.
    private double x0, y0;

    // Lambert conic first and second standard parallels in decimal degrees.
    private double p1, p2;

    // Transverse mercator central scale factor (1 - 1/SF)
    private double k0;

    public TransformParams(Context appContext, String code) {

        // TODO: Hook up units.
        units = Transforms.UNITS_SURVEY_FT;

        // TODO: Hook up local-grid transform.
        baseX = 5000.00;
        baseY = 10000.00;
        gridX = 6069017.11;
        gridY = 2118671.75;
        rotate = -1.2250;
        scale = 1.0;

        // Initialize new transform parameters from the Transform content provider.
        Cursor c = appContext.getContentResolver().query(
                Uri.parse(Projections.CONTENT_URI), // The content URI of the projections table
                null,                               // The columns to return for each row
                Projections.COLUMN_CODE + " = ?",   // Selection criteria
                new String[]{code},                 // Selection criteria
                null);                              // Sort order

        if (c != null && c.moveToFirst()) {

            Log.d(TAG, "projection: " + c.getString(Projections.INDEX_DESC)
                    + " (" + c.getString(Projections.INDEX_CODE) + ")");

            projection = c.getInt(Projections.INDEX_PROJECTION);
            p0 = c.getDouble(Projections.INDEX_P0);
            m0 = c.getDouble(Projections.INDEX_M0);
            x0 = c.getDouble(Projections.INDEX_X0);
            y0 = c.getDouble(Projections.INDEX_Y0);
            p1 = c.getDouble(Projections.INDEX_P1);
            p2 = c.getDouble(Projections.INDEX_P2);
            k0 = c.getDouble(Projections.INDEX_K0);
        }
    }

    public void setLocalTransform(
            int units,
            double baseX, double baseY,
            double gridX, double gridY,
            double rotate, double scale) {
        this.units = units;
        this.baseX = baseX; this.baseY = baseY;
        this.gridX = gridX; this.gridY = gridY;
        this.rotate = rotate;
        this.scale = scale;
    }

    public void setProjectionTM(
            double p0, double m0,
            double x0, double y0,
            int sf) {
        projection = Projections.PROJECTION_TM;
        this.p0 = p0; this.m0 = m0;
        this.x0 = x0; this.y0 = y0;
        this.k0 = (sf == 1) ? 1.0 : 1.0 - 1.0 / sf;
        this.p1 = 0.0; this.p2 = 0.0;
    }

    public void setProjectionLC(
            double p0, double m0,
            double x0, double y0,
            double p1, double p2) {
        projection = Projections.PROJECTION_LC;
        this.p0 = p0; this.m0 = m0;
        this.x0 = x0; this.y0 = y0;
        this.p0 = p1; this.p0 = p2;
        this.k0 = 0.0;
    }

    public int getUnits(){ return units; }
    public double getBaseX(){ return baseX; }
    public double getBaseY(){ return baseY; }
    public double getGridX(){ return gridX; }
    public double getGridY(){ return gridY; }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of TransformParams
        TransformParams params = (TransformParams) o;
        if (units == params.units
                && baseX == params.baseX && baseY == params.baseY
                && gridX == params.gridX && gridY == params.gridY
                && rotate == params.rotate
                && scale == params.scale
                && projection == params.projection
                && p0 == params.p0 && m0 == params.m0
                && x0 == params.m0 && y0 == params.y0
                && p1 == params.p1 && p2 == params.p2
                && k0 == params.k0) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {

        int vcode;
        long bits;
        
        int hash = 7;
        hash = 31 * hash + units;
        bits = Double.doubleToLongBits(baseX) ^ Double.doubleToLongBits(baseY);
        vcode = (int)(bits ^ (bits >> 32));
        hash = 31 * hash + vcode;
        bits = Double.doubleToLongBits(gridX) ^ Double.doubleToLongBits(gridY);
        vcode = (int)(bits ^ (bits >> 32));
        hash = 31 * hash + vcode;
        bits = Double.doubleToLongBits(rotate);
        vcode = (int)(bits ^ (bits >> 32));
        hash = 31 * hash + vcode;
        hash = 31 * hash + projection;
        bits = Double.doubleToLongBits(p0) ^ Double.doubleToLongBits(m0);
        vcode = (int)(bits ^ (bits >> 32));
        hash = 31 * hash + vcode;
        bits = Double.doubleToLongBits(x0) ^ Double.doubleToLongBits(y0);
        vcode = (int)(bits ^ (bits >> 32));
        hash = 31 * hash + vcode;

        return hash;
    }
}
