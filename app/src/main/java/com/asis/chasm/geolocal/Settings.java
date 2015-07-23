package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asis.chasm.geolocal.PointsContract.Projections;

public class Settings extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = "Settings";

    // Preference keys.
    public static final String PREFERENCE_KEY_UNITS = "pref_units";
    public static final String PREFERENCE_KEY_PROJECTION = "pref_projection";
    public static final String PREFERENCE_KEY_LOCAL_REF = "pref_local_ref";
    public static final String PREFERENCE_KEY_GEO_REF = "pref_geo_ref";
    public static final String PREFERENCE_KEY_ROTATION = "pref_rotation";
    public static final String PREFERENCE_KEY_SCALE = "pref_scale";

    // Display units preference values.
    public static final String PREFERENCE_UNITS_METERS = "meters";
    public static final String PREFERENCE_UNITS_SURVEY_FEET = "survey_feet";
    public static final String PREFERENCE_UNITS_INTERNATIONAL_FEET = "int_feet";

    // Required empty public constructor
    public Settings() { }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        Log.d(TAG, "onSharedPreferenceChanged key: " + key);
        Params.getParams().update(getActivity(), key);
        updatePreferenceSummary(findPreference(key));
    }

    private void initPreferenceSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGrp = (PreferenceGroup) pref;
            for (int i = 0; i < prefGrp.getPreferenceCount(); i++) {
                initPreferenceSummary(prefGrp.getPreference(i));
            }
        } else {
            updatePreferenceSummary(pref);
        }
    }

    /*
    * Update the preference lony on a per-preference basis.
    */
    private void updatePreferenceSummary(Preference pref) {
        Log.d(TAG, "updatePreferenceSummary key: " + pref.getKey());

        Params p = Params.getParams();
        switch (pref.getKey()) {

            case PREFERENCE_KEY_UNITS:
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());

                // Need to update the local reference lony when the units change.
                updatePreferenceSummary(findPreference(PREFERENCE_KEY_LOCAL_REF));
                break;

            case PREFERENCE_KEY_LOCAL_REF:
                LocalReferencePref coordsPref = (LocalReferencePref) pref;
                String[] localCoords = coordsPref.getValue().split(", ");
                if (localCoords.length == 2) {
                    double factor = p.getUnitsFactor();
                    double first = Double.parseDouble(localCoords[0]) * factor;
                    double second = Double.parseDouble(localCoords[1]) * factor;
                    String format = p.getLocalUnitsFormat() + ", " + p.getLocalUnitsFormat();
                    coordsPref.setSummary(String.format(format, first, second));
                }
                break;

            case PREFERENCE_KEY_ROTATION:
                RotationPref rotatePref = (RotationPref) pref;
                double rotation = Double.parseDouble(rotatePref.getValue());
                rotatePref.setSummary(String.format(p.getRotationUnitsFormat(), rotation));
                break;

            case PREFERENCE_KEY_GEO_REF:
                GeoReferencePref geoPref = (GeoReferencePref) pref;
                String[] geoCoords = geoPref.getValue().split(", ");
                if (geoCoords.length == 2) {
                    double first = Double.parseDouble(geoCoords[0]);
                    double second = Double.parseDouble(geoCoords[1]);
                    String format = p.getGeographicUnitsFormat() + ", " + p.getGeographicUnitsFormat();
                    geoPref.setSummary(String.format(format, first, second));
                }
                break;

            case PREFERENCE_KEY_PROJECTION:
                pref.setSummary(p.getProjectionDesc());
                break;
        }
    }

   @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);

        // Register the shared preference change listener.
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialize preference summaries.
        initPreferenceSummary(getPreferenceScreen());
    }

    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Unregister the shared preferences change listener.
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*
    * Params - a singleton to maintain app wide settings.
    */

    public static class Params {

        private static final String TAG = "Params";

        // Units names.
        private static final String UNITS_NAME_METERS = "meters";
        private static final String UNITS_NAME_SURVEY_FEET = "survey ft";
        private static final String UNITS_NAME_INTERNATIONAL_FEET = "international ft";
        private static final String UNITS_NAME_DEGREES = "degrees";

        private String mLocalUnitsName;
        public  String getLocalUnitsName() { return mLocalUnitsName; }
        public  String getGeographicUnitsName() { return UNITS_NAME_DEGREES; }
        public  String getRotationUnitsName() { return UNITS_NAME_DEGREES; }

        // Units suffixes.
        private static final String UNITS_ABBREV_METERS = "m";
        private static final String UNITS_ABBREV_SURVEY_FEET = "sft";
        private static final String UNITS_ABBREV_INTERNATIONAL_FEET = "int ft";
        private static final String UNITS_ABBREV_DEGREES = "deg";

        private String mLocalUnitsAbbrev;
        public  String getLocalUnitsAbbrev() { return mLocalUnitsAbbrev; }
        public  String getGeographicUnitsAbbrev() { return UNITS_ABBREV_DEGREES; }
        public  String getRotationUnitsAbbrev() { return UNITS_ABBREV_DEGREES; }

        // Units formats.
        private static final String UNITS_FORMAT_METERS = "%.3f";
        private static final String UNITS_FORMAT_SURVEY_FEET = "%.2f";
        private static final String UNITS_FORMAT_INTERNATIONAL_FEET = "%.2f";
        private static final String UNITS_FORMAT_GEOGRAPHIC = "%.8f";
        private static final String UNITS_FORMAT_ROTATION = "%.6f";

        private String mLocalUnitsFormat;
        public  String getLocalUnitsFormat() { return mLocalUnitsFormat; }
        public  String getGeographicUnitsFormat() { return UNITS_FORMAT_GEOGRAPHIC; }
        public  String getRotationUnitsFormat() { return UNITS_FORMAT_ROTATION; }

        // Conversion factors from internal system units (meters) to display units.
        public static final double UNITS_FACTOR_METERS = 1.0;
        public static final double UNITS_FACTOR_SURVEY_FEET = 3937.0 / 1200.0;
        public static final double UNITS_FACTOR_INTERNATIONAL_FEET = 1.0 / (0.0254 * 12);

        private double mLocalUnitsFactor;
        public  double getUnitsFactor() { return mLocalUnitsFactor; };

        // Local reference point in meters.
        private double refX, refY;
        public LocalPoint getLocalRef() { return new LocalPoint(refX, refY); }
        public  double getRefX() { return refX; }
        public  double getRefY() { return refY; }

        // Geographic reference point.
        private double refLat, refLon;
        public  double getRefLat() { return refLat; }
        public  double getRefLon() { return refLon; }

        // Grid reference point derived from geographic reference.
        private GridPoint gridRef = null;
        public GridPoint getGridRef() {
            if (gridRef == null) {
                gridRef = new GeoPoint(refLat, refLon).toGrid();
            }
            return gridRef;
        }
        public  void invalidateGridRef() { gridRef = null; }

        // Grid theta and scale factor at geographic reference.
        // These methods are quicker if the grid x/y coordinates are not needed.
        public double getGridTheta() { return new GeoPoint(refLat, refLon).getTheta(); }
        public double getGridSF() { return new GeoPoint(refLat, refLon).getK(); }

        // Rotation about reference point from local basis to grid in degrees.
        // A negative value rotates right (clockwise) from local to grid.
        private double rotation;
        public  double getRotation() { return rotation; }

        // Scale factor from local distances to geographic distance.
        private double scale;
        public  double getScale() { return scale; }

        // System of projections, e.g. SYSTEM_SPCS, UTM, USER.
        private int projectionSystem;
        public  int getProjectionSystem() { return projectionSystem; }

        // TODO: Make sure projection ID id updated on projections table reload.

        // Projection ID.
        private long projectionId;
        public  long getProjectionId() { return projectionId; }

        // Projection code and description.
        private String projectionCode, projectionDesc;
        public  String getProjectionCode() { return projectionCode; }
        public  String getProjectionDesc() { return projectionDesc; }

        // Type of projection, e.g. TYPE_TM, _LC, _OM.
        private int projectionType;
        public  int getProjectionType() { return projectionType; }

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
        private static Params sInstance = null;

        private Params() { }

        public static Params getParams() {
            return sInstance;
        }

        // Update settings for each key in the shared settings.
        public static void initialize(Context context) {
            Log.d(TAG, "initialize");

            if (sInstance == null) {
                sInstance = new Params();
            }

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            for (String key : sharedPrefs.getAll().keySet()){
                sInstance.update(context, key);
            }
            Log.d(TAG, "initialize complete");
        }

        public void update(Context context, String key) {

            // TODO: Figure out what to do with the local scale factor.
            scale = 1.0;

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String value = sharedPrefs.getString(key, "");
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Unset preference key: " + key);
            }

            Log.d(TAG, "update key=" + key + " value=" + value);

            switch (key) {
                case PREFERENCE_KEY_UNITS:
                    switch(value) {
                        case PREFERENCE_UNITS_METERS:
                            mLocalUnitsFactor = UNITS_FACTOR_METERS;
                            mLocalUnitsName = UNITS_NAME_METERS;
                            mLocalUnitsAbbrev = UNITS_ABBREV_METERS;
                            mLocalUnitsFormat = UNITS_FORMAT_METERS;
                            break;
                        case PREFERENCE_UNITS_SURVEY_FEET:
                            mLocalUnitsFactor = UNITS_FACTOR_SURVEY_FEET;
                            mLocalUnitsName = UNITS_NAME_SURVEY_FEET;
                            mLocalUnitsAbbrev = UNITS_ABBREV_SURVEY_FEET;
                            mLocalUnitsFormat = UNITS_FORMAT_SURVEY_FEET;
                            break;
                        case PREFERENCE_UNITS_INTERNATIONAL_FEET:
                            mLocalUnitsFactor = UNITS_FACTOR_INTERNATIONAL_FEET;
                            mLocalUnitsName = UNITS_NAME_INTERNATIONAL_FEET;
                            mLocalUnitsAbbrev = UNITS_ABBREV_INTERNATIONAL_FEET;
                            mLocalUnitsFormat = UNITS_FORMAT_INTERNATIONAL_FEET;
                            break;
                        default:
                            throw new IllegalArgumentException("Bad units setting: " + value);
                    }
                    break;

                case PREFERENCE_KEY_LOCAL_REF:
                    // Value is a comma separated coordinate pair formatted as y, x.
                    String[] localCoords = value.split(", ");
                    if (localCoords.length == 2) {
                        refY = Double.parseDouble(localCoords[0]);
                        refX = Double.parseDouble(localCoords[1]);
                    } else {
                        throw new IllegalArgumentException("Bad local reference coordinates: " + value);
                    }
                    break;

                case PREFERENCE_KEY_ROTATION:
                    rotation = Double.parseDouble(value);
                    break;

                case PREFERENCE_KEY_SCALE:
                    scale = Double.parseDouble(value);
                    break;

                case PREFERENCE_KEY_GEO_REF:
                    // Value is a comma separated coordinate pair formatted as lat, lon.
                    String[] geoCoords = value.split(", ");
                    if (geoCoords.length == 2) {
                        refLat = Double.parseDouble(geoCoords[0]);
                        refLon = Double.parseDouble(geoCoords[1]);
                        invalidateGridRef();
                    } else {
                        throw new IllegalArgumentException("Bad geographic reference coordinates: " + value);
                    }
                    break;

                case PREFERENCE_KEY_PROJECTION:
                    // Initialize projectionType constants from the Projections content provider.
                    Cursor c = context.getContentResolver().query(
                            Uri.parse(Projections.CONTENT_URI), // The content URI of the projections table
                            null,                               // Return all rows
                            Projections.COLUMN_CODE + " = ?",   // Select using the projectionType code
                            new String[]{value},                // Selection arguements
                            null);                              // No sort order

                    if (c != null && c.moveToFirst()) {

                        Log.d(TAG, "projectionType: " + c.getString(Projections.INDEX_DESC)
                                + " (" + c.getString(Projections.INDEX_CODE) + ")");

                        projectionId = c.getLong(Projections.INDEX_ID);
                        projectionSystem = c.getInt(Projections.INDEX_SYSTEM);
                        projectionType = c.getInt(Projections.INDEX_TYPE);
                        projectionCode = c.getString(Projections.INDEX_CODE);
                        projectionDesc = c.getString(Projections.INDEX_DESC);
                        p0 = c.getDouble(Projections.INDEX_P0);
                        m0 = c.getDouble(Projections.INDEX_M0);
                        x0 = c.getDouble(Projections.INDEX_X0);
                        y0 = c.getDouble(Projections.INDEX_Y0);
                        p1 = c.getDouble(Projections.INDEX_P1);
                        p2 = c.getDouble(Projections.INDEX_P2);
                        k0 = c.getDouble(Projections.INDEX_K0);
                        c.close();

                        invalidateGridRef();

                    } else {
                        throw new IllegalArgumentException("Bad projectionType code: " + value);
                    }

                    break;

                default:
                    Log.d(TAG, "Unknown preference key: " + key);
                    break;
            }
        }
    }
}
