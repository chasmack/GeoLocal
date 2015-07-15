package com.asis.chasm.geolocal;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.asis.chasm.geolocal.Settings.Params;

/*
* Coordinate pair preference.
*/

public class GeoReferencePref extends DialogPreference {

    private static final String TAG = "GeoReferencePref";

    // TODO: Add a "Read from GPX" button to read a waypoint from a GPX file.
    // TODO: Add a "Read from Location Service" option.

    // Geographic reference coordinates are saved in shared preferences
    // as a string formatted lat, lon.
    private static final String SHARED_PREFERENCES_COORD_FORMAT = "%.8f, %.8f";

    private static final String DEFAULT_VALUE = "0.00000000, 0.00000000";

    // Current value for the coordinate pair.
    private String mCurrentValue;
    public  String getValue() { return mCurrentValue; }

    // Reference to the dialog view.
    private View mDialogView;

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public GeoReferencePref(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_geo_ref);
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

        // Initialize the values.
        Params p = Params.getParams();
        ((EditText) view.findViewById(R.id.firstValue))
                .setText(String.format(p.getGeographicUnitsFormat(), p.getRefLat()));
        ((EditText) view.findViewById(R.id.secondValue))
                .setText(String.format(p.getGeographicUnitsFormat(), p.getRefLon()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {

            // TODO: Validate the user input for geographic reference.

            // Parse coordinate pair into doubles.
            double first = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.firstValue)).getText().toString());
            double second = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.secondValue)).getText().toString());

            // Format coordinate pair and persist to shared preferences.
            mCurrentValue = String.format(SHARED_PREFERENCES_COORD_FORMAT, first, second);
            persistString(mCurrentValue);

            Log.d(TAG, "onDialogClosed mCurrentValue: " + mCurrentValue);
        }
    }
}