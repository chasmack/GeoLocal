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
*  Rotation preference.
*/

public class RotationPref extends DialogPreference {

    private static final String TAG = "RotationPref";

    // Rotation saved in shared preferences as a formatted string in degrees.
    private static final String SHARED_PREFERENCES_COORD_FORMAT = "%.8f";

    private static final String DEFAULT_VALUE = "0.00000000";

    // Current value.
    private String mCurrentValue;
    public  String getValue() { return mCurrentValue; }

    // Reference to the dialog view.
    private View mDialogView = null;

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public RotationPref(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_rotation);
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

        // Save the dialog view.
        mDialogView = view;

        // Initialize the value with the current value.
        EditText v = (EditText) view.findViewById(R.id.value);
        Params p = Params.getParams();
        v.setText(String.format(p.getRotationUnitsFormat(), p.getRotation()));

//        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                }
//            }
//        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {

            // TODO: Validate the user input for the rotation.

            // Parse the preference value into a double.
            double value = Double.parseDouble(((EditText) mDialogView
                    .findViewById(R.id.value)).getText().toString());

            // Format and persist to the shared preferences.
            mCurrentValue = String.format(SHARED_PREFERENCES_COORD_FORMAT, value);
            persistString(mCurrentValue);

            Log.d(TAG, "onDialogClosed mCurrentValue: " + mCurrentValue);
        }
    }
}