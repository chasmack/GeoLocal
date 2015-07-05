package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asis.chasm.geolocal.PointsContract.Transforms;

import java.util.prefs.Preferences;

public class TransformSettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    // String constants for the preference.
    public static final String PREFERENCE_SWITCH_KEY = "pref_switch";
    public static final String PREFERENCE_UNITS_KEY = "pref_units";

    public static final String PREFERENCE_UNITS_METRIC = "metric";
    public static final String PREFERENCE_UNITS_SURVEY_FEET = "survey_feet";
    public static final String PREFERENCE_UNITS_INTERNATIONAL_FEET = "international_feet";

    public TransformSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        Log.d(TAG, "onSharedPreferenceChanged key: " + key);
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

            case PREFERENCE_SWITCH_KEY:
                pref.setSummary(((SwitchPreference) pref).isChecked() ?
                        R.string.pref_switch_summary_checked :
                        R.string.pref_switch_summary_not_checked);
                break;

            case PREFERENCE_UNITS_KEY:
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
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

        SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();

        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(
                (PointsManagerFragment) getFragmentManager()
                        .findFragmentByTag(MainActivity.FRAGMENT_POINTS_MANAGER));
    }

    @Override
    public void onPause () {
        super.onPause();

        SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();

        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(
                (PointsManagerFragment) getFragmentManager()
                        .findFragmentByTag(MainActivity.FRAGMENT_POINTS_MANAGER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
