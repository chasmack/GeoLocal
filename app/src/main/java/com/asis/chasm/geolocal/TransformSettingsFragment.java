package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class TransformSettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

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
    public TransformSettingsFragment() { }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        Log.d(TAG, "onSharedPreferenceChanged key: " + key);
        TransformSettings.getSettings().update(getActivity(), key);
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
    * Update the preference summary on a per-preference basis.
    */
    private void updatePreferenceSummary(Preference pref) {
        Log.d(TAG, "updatePreferenceSummary key: " + pref.getKey());

        TransformSettings s = TransformSettings.getSettings();
        switch (pref.getKey()) {

            case PREFERENCE_KEY_UNITS:
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());

                // Local reference summary needs update when the units change.
                updatePreferenceSummary(
                    findPreference(PREFERENCE_KEY_LOCAL_REF));

                break;

            case PREFERENCE_KEY_LOCAL_REF:
                LocalRefPreference coordsPref = (LocalRefPreference) pref;
                String[] localCoords = coordsPref.getValue().split(", ");
                if (localCoords.length == 2) {
                    double factor = s.getUnitsFactor();
                    double first = Double.parseDouble(localCoords[0]) * factor;
                    double second = Double.parseDouble(localCoords[1]) * factor;
                    String format = s.getLocalUnitsFormat() + ", " + s.getLocalUnitsFormat();
                    coordsPref.setSummary(String.format(format, first, second));
                }
                break;

            case PREFERENCE_KEY_ROTATION:
                RotationPreference rotatePref = (RotationPreference) pref;
                double rotation = Double.parseDouble(rotatePref.getValue());
                rotatePref.setSummary(String.format(s.getRotationUnitsFormat(), rotation));
                break;

            case PREFERENCE_KEY_GEO_REF:
                GeoRefPreference geoPref = (GeoRefPreference) pref;
                String[] geoCoords = geoPref.getValue().split(", ");
                if (geoCoords.length == 2) {
                    double first = Double.parseDouble(geoCoords[0]);
                    double second = Double.parseDouble(geoCoords[1]);
                    String format = s.getGeographicUnitsFormat() + ", " + s.getGeographicUnitsFormat();
                    geoPref.setSummary(String.format(format, first, second));
                }
                break;

            case PREFERENCE_KEY_PROJECTION:
                pref.setSummary(TransformSettings.getSettings().getProjectionDesc());
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

        // Initialize preference summaries and register the change listener.
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
}
