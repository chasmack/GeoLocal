package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TransformSettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

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

            case TransformSettings.PREFERENCE_KEY_UNITS:
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());

                // Local reference summary needs update when the units change.
                updatePreferenceSummary(
                    findPreference(TransformSettings.PREFERENCE_KEY_LOCAL_REF));

                break;

            case TransformSettings.PREFERENCE_KEY_LOCAL_REF:
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

            case TransformSettings.PREFERENCE_KEY_ROTATION:
                RotationPreference rotatePref = (RotationPreference) pref;
                double rotation = Double.parseDouble(rotatePref.getValue());
                rotatePref.setSummary(String.format(s.getRotationUnitsFormat(), rotation));
                break;

            case TransformSettings.PREFERENCE_KEY_GEO_REF:
                GeoRefPreference geoPref = (GeoRefPreference) pref;
                String[] geoCoords = geoPref.getValue().split(", ");
                if (geoCoords.length == 2) {
                    double first = Double.parseDouble(geoCoords[0]);
                    double second = Double.parseDouble(geoCoords[1]);
                    String format = s.getGeographicUnitsFormat() + ", " + s.getGeographicUnitsFormat();
                    geoPref.setSummary(String.format(format, first, second));
                }
                break;

            case TransformSettings.PREFERENCE_KEY_PROJECTION:
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

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
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

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause () {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
