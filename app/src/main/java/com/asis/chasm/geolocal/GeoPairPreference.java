package com.asis.chasm.geolocal;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/*
* Coordinate pair preference.
*/

public class GeoPairPreference extends DialogPreference {

    private static final String TAG = "GeoPairPreference";

    // Reference to the dialog view.
    private View mDialogView;

    private static final String DEFAULT_VALUE = "0.00000000, 0.00000000";

    // Current value for the coordinate pair.
    private String mCurrentValue;
    public  String getValue() { return mCurrentValue; }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public GeoPairPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_geo_pair);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
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

        // Save a reference to the dialog view.
        mDialogView = view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {

            // TODO: Validate the user input.

            TransformSettings settings = TransformSettings.getSettings();
            double factor = settings.getUnitsFactor();

            // Parse coordinate pair into doubles.
            double first = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.firstValue)).getText().toString());
            double second = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.secondValue)).getText().toString());

            // Format coordinate pair and persist to shared preferences.
            mCurrentValue = String.format(settings.getGeographicCoordFormat(), first, second);
            persistString(mCurrentValue);

            Log.d(TAG, "onDialogClosed mCurrentValue: " + mCurrentValue);
        }
    }
}