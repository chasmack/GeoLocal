package com.asis.chasm.geolocal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/*
* Coordinate pair preference.
*/

public class CoordPairPreference extends DialogPreference {

    private static final String TAG = "CoordPairPreference";

    // References to the edit text views containing the coordinate values.
    private EditText mFirstValue, mSecondValue;

    private static final String DEFAULT_VALUE = "0.00, 0.00";

    // Current value for the coordinate pair.
    private String mCurrentValue;
    public  String getValue() { return mCurrentValue; }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public CoordPairPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_coord_pair);
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

        // Save references to the coordinate values.
        mFirstValue = (EditText) view.findViewById(R.id.firstCoordValue);
        mSecondValue = (EditText) view.findViewById(R.id.secondCoordValue);

        // Set the units suffix text.
        String suffix = TransformSettings.getSettings().getLocalCoordSuffix();
        ((TextView)view.findViewById(R.id.firstCoordSuffix)).setText(suffix);
        ((TextView)view.findViewById(R.id.secondCoordSuffix)).setText(suffix);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {

            TransformSettings settings = TransformSettings.getSettings();
            double factor = settings.getUnitsFactor();
            Double first = Double.parseDouble(mFirstValue.getText().toString()) / factor;
            Double second = Double.parseDouble(mSecondValue.getText().toString()) / factor;

            mCurrentValue = String.format(TransformSettings.LOCAL_COORD_FORMAT_METERS, first, second);
            persistString(mCurrentValue);
        }
    }
}