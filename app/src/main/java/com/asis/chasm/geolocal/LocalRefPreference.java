package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v4.app.NavUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
* Coordinate pair preference.
*/

public class LocalRefPreference extends DialogPreference {

    // TODO: Add a "Pick point" button to preference dialog to pick a local point.

    private static final String TAG = "LocalRefPreference";

    // Local reference coordinates are converted from display units into
    // meters and saved in shared preferences as a string formatted y, x.
    private static final String SHARED_PREFERENCES_COORD_FORMAT = "%.4f, %.4f";

    private static final String DEFAULT_VALUE = "0.0000, 0.0000";

    // Current value for the coordinate pair.
    private String mCurrentValue;
    public  String getValue() { return mCurrentValue; }

    // Reference to the dialog view.
    private View mDialogView;

    // Callback interface for the dialog button.
    interface PreferenceListener {
        void selectLocalPoint();
    }
    PreferenceListener mListener = null;

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public LocalRefPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_local_ref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

        mListener = (PreferenceListener) context;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        Log.d(TAG, "onSetInitialValue persisted: " + restorePersistedValue);

        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedString(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (String) defaultValue;
            persistString(mCurrentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String value = a.getString(index);
        return value != null ? value : DEFAULT_VALUE;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // Save references to the dialog view.
        mDialogView = view;

        // Set the units suffix.
        TransformSettings s = TransformSettings.getSettings();
        ((TextView) view.findViewById(R.id.firstSuffix)).setText(s.getLocalUnitsAbbrev());
        ((TextView) view.findViewById(R.id.secondSuffix)).setText(s.getLocalUnitsAbbrev());

        // Initialize the values.
        ((EditText) view.findViewById(R.id.firstValue))
                .setText(String.format(s.getLocalUnitsFormat(), s.getRefY() * s.getUnitsFactor()));
        ((EditText) view.findViewById(R.id.secondValue))
                .setText(String.format(s.getLocalUnitsFormat(), s.getRefX() * s.getUnitsFactor()));

        // Select a point button callback
        view.findViewById(R.id.buttonSelectPoint)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null ) {
                            mListener.selectLocalPoint();
                        }
                    }
                });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {

            // TODO: Validate the user input for the local reference.

            TransformSettings s = TransformSettings.getSettings();
            double factor = s.getUnitsFactor();

            // Parse coordinate pair into doubles and convert to meters.
            double first = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.firstValue)).getText().toString()) / factor;
            double second = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.secondValue)).getText().toString()) / factor;

            // Format coordinate pair and persist to shared preferences.
            mCurrentValue = String.format(SHARED_PREFERENCES_COORD_FORMAT, first, second);
            persistString(mCurrentValue);

            Log.d(TAG, "onDialogClosed mCurrentValue: " + mCurrentValue);
        }
    }
}