package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransformSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */

public class TransformSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private OnFragmentInteractionListener mListener;

    public TransformSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {

            case "pref_switch":
                getPreferenceScreen().findPreference(key)
                        .setSummary(prefs.getBoolean(key, false) ?
                                R.string.pref_switch_summary_true :
                                R.string.pref_switch_summary_false );
                break;

            case "pref_units":
                Preference pref = findPreference(key);
                pref.setSummary(((ListPreference)pref).getEntry());
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
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

        // Set preference summaries based on current values.
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        SwitchPreference switchPref = (SwitchPreference) getPreferenceScreen().findPreference("pref_switch");
        if (switchPref.isChecked()) {
            switchPref.setSummary(getString(R.string.pref_switch_summary_true));
        } else {
            switchPref.setSummary(getString(R.string.pref_switch_summary_false));
        }

        ListPreference unitsPref = (ListPreference) getPreferenceScreen().findPreference("pref_units");
        unitsPref.setSummary(unitsPref.getEntry());

        prefs.registerOnSharedPreferenceChangeListener(this);
        prefs.registerOnSharedPreferenceChangeListener((MainActivity) getActivity());
    }

    @Override
        public void onPause () {
            super.onPause();
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        prefs.unregisterOnSharedPreferenceChangeListener((MainActivity) getActivity());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onTransformSettingsFragmentInteraction(int value);
    }

}
