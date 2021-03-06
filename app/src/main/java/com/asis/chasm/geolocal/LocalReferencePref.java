package com.asis.chasm.geolocal;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.asis.chasm.geolocal.Settings.Params;

/*
* Coordinate pair preference.
*/

public class LocalReferencePref extends DialogPreference {

    private static final String TAG = "LocalReferencePref";

    // Local reference coordinates are converted from display units into
    // meters and saved in shared preferences as a string formatted y, x.
    public static final String SHARED_PREFERENCES_COORD_FORMAT = "%.4f, %.4f";

    private static final String DEFAULT_VALUE = "0.0000, 0.0000";

    // Current value for the coordinate pair.
    private String mCurrentValue;
    public  String getValue() { return this.getPersistedString(DEFAULT_VALUE); }

    // Reference to the dialog view.
    private View mDialogView;

    // Callback interface for the dialog button.
    interface OnPreferenceInteractionListener {
        void onSelectLocalRefPoint(Dialog dialog, View view);
    }
    OnPreferenceInteractionListener mListener = null;

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);
    }

    public LocalReferencePref(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        setDialogLayoutResource(R.layout.dialog_local_ref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

        mListener = (OnPreferenceInteractionListener) context;
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
        Params p = Params.getParams();
        ((TextView) view.findViewById(R.id.firstSuffix)).setText(p.getLocalUnitsAbbrev());
        ((TextView) view.findViewById(R.id.secondSuffix)).setText(p.getLocalUnitsAbbrev());

        // Initialize the values.
        ((EditText) view.findViewById(R.id.firstValue))
                .setText(String.format(p.getLocalUnitsFormat(), p.getRefY() * p.getUnitsFactor()));
        ((EditText) view.findViewById(R.id.secondValue))
                .setText(String.format(p.getLocalUnitsFormat(), p.getRefX() * p.getUnitsFactor()));

        // Select a point button callback
        view.findViewById(R.id.buttonSelectPoint)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null ) {
                            mListener.onSelectLocalRefPoint(getDialog(), mDialogView);
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

            Params p = Params.getParams();
            double factor = p.getUnitsFactor();

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