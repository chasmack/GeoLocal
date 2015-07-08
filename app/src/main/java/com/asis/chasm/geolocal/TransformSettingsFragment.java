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

import com.asis.chasm.geolocal.PointsContract.Transforms;

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
                break;

            case TransformSettings.PREFERENCE_KEY_LOCAL_BASE:
                CoordPairPreference coordsPref = (CoordPairPreference) pref;
                String[] coords = coordsPref.getValue().split(", ");
                if (coords.length == 2) {
                    TransformSettings settings = TransformSettings.getSettings();
                    double factor = settings.getUnitsFactor();
                    double first = Double.parseDouble(coords[0]) * factor;
                    double second = Double.parseDouble(coords[1]) * factor;
                    coordsPref.setSummary(String.format(settings.getLocalCoordFormat(), first, second));
                }
                break;

            case TransformSettings.PREFERENCE_KEY_PROJECTION:
                pref.setSummary(TransformSettings.getSettings().getProjectionDesc());
                break;

            case TransformSettings.PREFERENCE_KEY_SWITCH:
                pref.setSummary(((SwitchPreference) pref).isChecked() ?
                        R.string.pref_switch_summary_checked :
                        R.string.pref_switch_summary_not_checked);
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
