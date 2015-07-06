package com.asis.chasm.geolocal;

import android.content.Context;
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

    private EditText mFirstValue, mSecondValue;
    private String mUnits;

    private static final String DEFAULT_VALUE = "0.00, 0.00";

    private String mCurrentValue;

    public String getValue() { return mCurrentValue; }

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

        // Save current value of the units preference and set the units suffix.
        String key = getContext().getString(R.string.pref_units_key);
        ListPreference unitsPref = (ListPreference) findPreferenceInHierarchy(key);
        mUnits = unitsPref.getValue();

        String suffix = "";
        switch (mUnits) {
            case TransformSettingsFragment.PREFERENCE_UNITS_METRIC:
                suffix = "m";
                break;
            case TransformSettingsFragment.PREFERENCE_UNITS_SURVEY_FEET:
                suffix = "sft";
                break;
            case TransformSettingsFragment.PREFERENCE_UNITS_INTERNATIONAL_FEET:
                suffix = "ft (international)";
                break;
        }
        ((TextView)view.findViewById(R.id.firstCoordUnits)).setText(suffix);
        ((TextView)view.findViewById(R.id.secondCoordUnits)).setText(suffix);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {
            String format = mUnits == TransformSettingsFragment.PREFERENCE_UNITS_METRIC ?
                    "%.3f, %.3f" : "%.2f, %.2f";

            mCurrentValue = String.format(format,
                    Double.parseDouble(mFirstValue.getText().toString()),
                    Double.parseDouble(mSecondValue.getText().toString()));

            persistString(mCurrentValue);
        }
    }
}