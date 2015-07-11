package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
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

        switch (pref.getKey()) {

            case TransformSettings.PREFERENCE_KEY_UNITS:
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());

                // Local base coordinate summary needs update when units change.
                updatePreferenceSummary(
                    findPreference(TransformSettings.PREFERENCE_KEY_LOCAL));

                break;

            case TransformSettings.PREFERENCE_KEY_LOCAL:
                CoordPairPreference coordsPref = (CoordPairPreference) pref;
                String[] localPair = coordsPref.getValue().split(", ");
                if (localPair.length == 2) {
                    TransformSettings settings = TransformSettings.getSettings();
                    double factor = settings.getUnitsFactor();
                    double first = Double.parseDouble(localPair[0]) * factor;
                    double second = Double.parseDouble(localPair[1]) * factor;
                    String summary = String.format(settings.getLocalCoordFormat(), first, second);
                    coordsPref.setSummary(summary);
                }
                break;

            case TransformSettings.PREFERENCE_KEY_ROTATION:
                RotationPreference rotatePref = (RotationPreference) pref;
                rotatePref.setSummary(rotatePref.getValue());
                break;

            case TransformSettings.PREFERENCE_KEY_GEO:
                GeoPairPreference geoPref = (GeoPairPreference) pref;
                String[] geoPair = geoPref.getValue().split(", ");
                if (geoPair.length == 2) {
                    TransformSettings settings = TransformSettings.getSettings();
                    double first = Double.parseDouble(geoPair[0]);
                    double second = Double.parseDouble(geoPair[1]);
                    String summary = String.format(settings.getGeographicCoordFormat(), first, second);
                    geoPref.setSummary(summary);
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
