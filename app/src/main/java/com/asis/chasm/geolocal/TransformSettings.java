package com.asis.chasm.geolocal;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;

import com.asis.chasm.geolocal.PointsContract.Projections;

/**
 * Transform settings.
 */
public class TransformSettings {

    private static final String TAG = "TransformSettings";

    // Preference keys.
    public static final String PREFERENCE_KEY_UNITS = "pref_units";
    public static final String PREFERENCE_KEY_LOCAL_BASE = "pref_local_base";
    public static final String PREFERENCE_KEY_ROTATE = "pref_rotate";
    public static final String PREFERENCE_KEY_SCALE = "pref_scale";
    public static final String PREFERENCE_KEY_GRID_BASE = "pref_geo_base";
    public static final String PREFERENCE_KEY_PROJECTION = "pref_projection";

    // Display units preference values.
    public static final String PREFERENCE_UNITS_METERS = "meters";
    public static final String PREFERENCE_UNITS_SURVEY_FEET = "survey_feet";
    public static final String PREFERENCE_UNITS_INTERNATIONAL_FEET = "int_feet";

    private String mDisplayUnits;
    public  String getDisplayUnits() { return mDisplayUnits; }

    // Units names.
    public static final String UNITS_NAME_METERS = "meters";
    public static final String UNITS_NAME_SURVEY_FEET = "survey ft";
    public static final String UNITS_NAME_INTERNATIONAL_FEET = "international ft";
    public static final String UNITS_NAME_GEOGRAPHIC = "degrees";

    private String mUnitsName;
    public  String getUnitsName() { return mUnitsName; }

    // Coordinate suffixes.
    public static final String LOCAL_COORD_SUFFIX_METERS = "m";
    public static final String LOCAL_COORD_SUFFIX_SURVEY_FEET = "sft";
    public static final String LOCAL_COORD_SUFFIX_INTERNATIONAL_FEET = "int ft";

    private String mLocalCoordSuffix;
    public  String getLocalCoordSuffix() { return mLocalCoordSuffix; }

    // Local coordinate pair formats.
    public static final String LOCAL_COORD_FORMAT_METERS = "%.3f, %.3f";
    public static final String LOCAL_COORD_FORMAT_SURVEY_FEET = "%.2f, %.2f";
    public static final String LOCAL_COORD_FORMAT_INTERNATIONAL_FEET = "%.2f, %.2f";

    private String mLocalCoordFormat;
    public  String getLocalCoordFormat() { return mLocalCoordFormat; }

    // Geographic coordinate pair format.
    public static final String GEOGRAPHIC_COORD_FORMAT = "%.6f, %.6f";

    public  String getGeographicCoordFormat() { return GEOGRAPHIC_COORD_FORMAT; }

    // Conversion factors from internal system units (meters) to display units.
    public static final double UNITS_FACTOR_METERS = 1.0;
    public static final double UNITS_FACTOR_SURVEY_FEET = 3937.0 / 1200.0;
    public static final double UNITS_FACTOR_INTERNATIONAL_FEET = 1.0 / (0.0254 * 12);

    private double mUnitsFactor;
    public  double getUnitsFactor() { return mUnitsFactor; };

    // Local base point coordinates in meters.
    private double baseX, baseY;
    public  double getBaseX() { return baseX; }
    public  double getBaseY() { return baseY; }

    // Grid reference point coordinates in meters.
    private double gridX, gridY;
    public  double getGridX() { return gridX; }
    public  double getGridY() { return gridY; }

    // Rotation about reference point from local basis to grid in degrees.
    // A negative value rotates right (clockwise) from local to grid.
    private double rotate;
    public  double getRotate() { return rotate; }

    // Scale factor from local distances to geographic distance.
    private double scale;
    public  double getScale() { return scale; }

    // Type of grid projection, e.g. TM, LC, OM.
    private int projection;
    public  int getProjection() { return projection; }

    // Projection code and description.
    private String projectionCode, projectionDesc;
    public  String getProjectionCode() { return projectionCode; }
    public  String getProjectionDesc() { return projectionDesc; }

    // Latitude of origin, central meridian in degrees.
    private double p0, m0;
    public  double getP0() { return p0; }
    public  double getM0() { return m0; }

    // False easting, northing in meters.
    private double x0, y0;
    public  double getX0() { return x0; }
    public  double getY0() { return y0; }

    // Lambert conic first and second standard parallels in degrees.
    private double p1, p2;
    public  double getP1() { return p1; }
    public  double getP2() { return p2; }

    // Transverse mercator central scale factor (1 - 1/SF)
    private double k0;
    public  double getK0() { return k0; }

    // Singleton reference.
    private static TransformSettings sInstance = null;

    private TransformSettings() { }

    public static TransformSettings getSettings() {
        return sInstance;
    }

    // Update settings for each key in the shared settings.
    public static void initialize(Context context) {
        Log.d(TAG, "initialize");

        if (sInstance == null) {
            sInstance = new TransformSettings();
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Map<String, ?> prefs = sharedPrefs.getAll();
        for (String key : prefs.keySet()) {
            sInstance.update(context, key);
        }
    }

    public void update(Context context, String key) {

        // TODO: Hook up transform settings.
        gridX = 6069017.11 / UNITS_FACTOR_SURVEY_FEET;
        gridY = 2118671.75 / UNITS_FACTOR_SURVEY_FEET;
        rotate = -1.2250;
        scale = 1.0;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String value = sharedPrefs.getString(key, "");
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Unset preference key: " + key);
        }

        Log.d(TAG, "update key: " + key + "=" + value);

        switch (key) {
            case PREFERENCE_KEY_UNITS:
                switch(value) {
                    case PREFERENCE_UNITS_METERS:
                        mUnitsFactor = UNITS_FACTOR_METERS;
                        mUnitsName = UNITS_NAME_METERS;
                        mLocalCoordSuffix = LOCAL_COORD_SUFFIX_METERS;
                        mLocalCoordFormat = LOCAL_COORD_FORMAT_METERS;
                        break;
                    case PREFERENCE_UNITS_SURVEY_FEET:
                        mUnitsFactor = UNITS_FACTOR_SURVEY_FEET;
                        mUnitsName = UNITS_NAME_SURVEY_FEET;
                        mLocalCoordSuffix = LOCAL_COORD_SUFFIX_SURVEY_FEET;
                        mLocalCoordFormat = LOCAL_COORD_FORMAT_SURVEY_FEET;
                        break;
                    case PREFERENCE_UNITS_INTERNATIONAL_FEET:
                        mUnitsFactor = UNITS_FACTOR_INTERNATIONAL_FEET;
                        mUnitsName = UNITS_NAME_INTERNATIONAL_FEET;
                        mLocalCoordSuffix = LOCAL_COORD_SUFFIX_INTERNATIONAL_FEET;
                        mLocalCoordFormat = LOCAL_COORD_FORMAT_INTERNATIONAL_FEET;
                        break;
                    default:
                        throw new IllegalArgumentException("Bad units setting: " + value);
                }
                break;

            case PREFERENCE_KEY_LOCAL_BASE:
                String[] coords = value.split(", ");
                if (coords.length == 2) {
                    baseX = Double.parseDouble(coords[0]);
                    baseY = Double.parseDouble(coords[1]);
                } else {
                    throw new IllegalArgumentException("Bad local base coordinates: " + value);
                }
                break;

            case PREFERENCE_KEY_ROTATE:
                break;

            case PREFERENCE_KEY_SCALE:
                break;

            case PREFERENCE_KEY_GRID_BASE:
                break;

            case PREFERENCE_KEY_PROJECTION:
                // Initialize projection constants from the Projections content provider.
                Cursor c = context.getContentResolver().query(
                        Uri.parse(Projections.CONTENT_URI), // The content URI of the projections table
                        null,                               // Return all rows
                        Projections.COLUMN_CODE + " = ?",   // Select using the projection code
                        new String[]{value},                // Selection arguements
                        null);                              // No sort order

                if (c != null && c.moveToFirst()) {

                    Log.d(TAG, "projection: " + c.getString(Projections.INDEX_DESC)
                            + " (" + c.getString(Projections.INDEX_CODE) + ")");

                    projection = c.getInt(Projections.INDEX_PROJECTION);
                    projectionCode = c.getString(Projections.INDEX_CODE);
                    projectionDesc = c.getString(Projections.INDEX_DESC);
                    p0 = c.getDouble(Projections.INDEX_P0);
                    m0 = c.getDouble(Projections.INDEX_M0);
                    x0 = c.getDouble(Projections.INDEX_X0);
                    y0 = c.getDouble(Projections.INDEX_Y0);
                    p1 = c.getDouble(Projections.INDEX_P1);
                    p2 = c.getDouble(Projections.INDEX_P2);
                    k0 = c.getDouble(Projections.INDEX_K0);

                } else {
                    throw new IllegalArgumentException("Bad projection code: " + value);
                }
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        // object is a non-null instance of TransformSettings
        TransformSettings xp = (TransformSettings) o;
        if (baseX == xp.baseX && baseY == xp.baseY
                && gridX == xp.gridX && gridY == xp.gridY
                && rotate == xp.rotate
                && scale == xp.scale
                && projection == xp.projection
                && p0 == xp.p0 && m0 == xp.m0
                && x0 == xp.m0 && y0 == xp.y0
                && p1 == xp.p1 && p2 == xp.p2
                && k0 == xp.k0) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {

        int vcode;
        long bits;
        
        int hash = 7;
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
