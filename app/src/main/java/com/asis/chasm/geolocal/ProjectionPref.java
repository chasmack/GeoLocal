package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.asis.chasm.geolocal.Settings.Params;
import com.asis.chasm.geolocal.PointsContract.Projections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
*  Projection preference.
*/

public class ProjectionPref extends DialogPreference implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "ProjectionPref";

    private static final String DEFAULT_PROJECTION = "0401";

    // Current values for projection system and code.
    // These are initialized to the current saved settings upon entry
    // and are updated as the user selects new values with the spinners.
    // ProjectionCode is persisted to shared preferences when the dialog
    // is closed with a positive result, i.e. OK is selected.
    private int mCurrentProjectionSystem;
    private String mCurrentProjectionCode;

    public  String getValue() { return mCurrentProjectionCode; }

    // References to the spinners.
    private Spinner mSystemsSpinner;
    private Spinner mProjectionsSpinner;

    // Reference to the application context.
    private Context mContext;

    // Reference to the main activity's loader manager.
    private LoaderManager mLoaderManager;

    // Custom array adapter for the systems spinner.
    private SystemsAdapter mSystemsAdapter;

    // Custom cursor adapter for the projections spinner.
    private ProjectionsAdapter mProjectionsAdapter;

    // Recents hash mapping coordinate system id to last projection code selected.
    private Map<Integer, String> mRecents;

    /*
    * Constructor.
    */

    public ProjectionPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Constructor");

        mContext = context.getApplicationContext();

        setDialogLayoutResource(R.layout.dialog_projection);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

        // The assumption here is the context getting passed around here
        // is actually our MainActivity.
        //
        // We need to use the Activity's loader manager since the
        // class can be instantiated without the PreferencesFragment
        // when the preference defaults need to be set for the first time.
        mLoaderManager = ((Activity) context).getLoaderManager();

        // A map of the last projection id for each system id.
        mRecents = new HashMap<Integer, String>();
    }

    @Override
    protected View onCreateDialogView() {
        View view =  super.onCreateDialogView();
        Log.d(TAG, "onCreateDialogView");

        // Reinitialize the current values for system and projection.
        Params p = Params.getParams();
        mCurrentProjectionSystem = p.getProjectionSystem();
        mCurrentProjectionCode = p.getProjectionCode();
        mRecents.put(mCurrentProjectionSystem, mCurrentProjectionCode);

        // Get new references to the two spinners.
        mSystemsSpinner = (Spinner) view.findViewById(R.id.systems_spinner);
        mProjectionsSpinner = (Spinner) view.findViewById(R.id.projections_spinner);

        // Create an array adapter for the systems spinner.
        if (mSystemsAdapter == null) {
            List<Integer> systems = new ArrayList<Integer>();
            for (int i : Projections.SYSTEM_IDS) { systems.add(i); }
            mSystemsAdapter = new SystemsAdapter(mContext, R.layout.spinner_item, systems);
        }

        // Bind the systems adapter to the spinner and set the selection.
        mSystemsSpinner.setAdapter(mSystemsAdapter);
        mSystemsSpinner.setSelection(mSystemsAdapter.getPosition(mCurrentProjectionSystem));

        // Create a cursor adapter for the projections spinner.
        if (mProjectionsAdapter == null) {
            mProjectionsAdapter = new ProjectionsAdapter(mContext, null, 0);
        }

        // Connect the projections adapter to the spinner.
        mProjectionsSpinner.setAdapter(mProjectionsAdapter);

        // Check the state of the loader.
        if (mLoaderManager.getLoader(MainActivity.LOADER_ID_PREF_PROJECTIONS) == null) {

            // First time through we need to create a new loader.
            mLoaderManager.initLoader(MainActivity.LOADER_ID_PREF_PROJECTIONS, null, this);
        } else {
            // Otherwise restart the existing loader.
            mLoaderManager.restartLoader(MainActivity.LOADER_ID_PREF_PROJECTIONS, null, this);
        }

        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Log.d(TAG, "onBindDialogView");

        // Set listeners for the spinners.
        mSystemsSpinner.setOnItemSelectedListener(this);
        mProjectionsSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Log.d(TAG, "onDialogClosed result=" + positiveResult + " code=" + mCurrentProjectionCode);

        if (positiveResult) {
            persistString(mCurrentProjectionCode);
        }
    }

    /*
    * Array adapter for systems spinner.
    */

    private class SystemsAdapter extends ArrayAdapter<Integer> {

        public SystemsAdapter(Context context, int layoutId, List<Integer> items) {
            super(context, layoutId, items);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Log.d(TAG, "SyatemsAdapter getView");

            // Check if an existing view is being reused, otherwise inflate the view
            if (view == null) {
                view = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.spinner_item, parent, false);
            }

            TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(Projections.SYSTEM_NAMES[getItem(position)]);

            return view;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            Log.d(TAG, "SystemsAdapter getDropDownView");

            // Check if an existing view is being reused, otherwise inflate the view
            if (view == null) {
                view = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.spinner_dropdown_item, parent, false);
            }

            TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(Projections.SYSTEM_NAMES[getItem(position)]);

            return view;
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        Log.d(TAG, "onSetInitialValue persisted=" + restorePersistedValue);

        if (restorePersistedValue) {
            // Restore existing state
            mCurrentProjectionCode = this.getPersistedString(DEFAULT_PROJECTION);
        } else {
            // Set default state from the XML attribute
            mCurrentProjectionCode = (String) defaultValue;
            persistString(mCurrentProjectionCode);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String value = a.getString(index);
        return value != null ? value : DEFAULT_PROJECTION;
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    /*
    * AdapterView.OnItemSelectedListener implementation.
    */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.systems_spinner:
                Log.d(TAG, "onItemSelected systems pos=" + position + " id=" + id);

                // Update the current projection system.
                mCurrentProjectionSystem = position;

                // Flag the current projection code as invalid and restart the loader.
                // When the loader is finished it will update the current projection.
                // If there is a code in recents for the selected system that will be used.
                // Otherwise the code will be set to the code at position 0.
                mCurrentProjectionCode = null;
                mLoaderManager.restartLoader(MainActivity.LOADER_ID_PREF_PROJECTIONS, null, this);
                break;

            case R.id.projections_spinner:
                Log.d(TAG, "onItemSelected projections pos=" + position + " id=" + id);

                // Update the current projection code and the recents.
                mCurrentProjectionCode = ((TextView)mProjectionsSpinner
                        .getSelectedView().findViewById(R.id.code)).getText().toString();

                mRecents.put(mCurrentProjectionSystem, mCurrentProjectionCode);
                break;

            default:
                Log.d(TAG, "onItemSelected DEFAULT pos=" + position + " id=" + id);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "AdapterView.OnItemSelectedListener.onNothingSelected");
    }

    /*
    * LoaderManager.LoaderCallbacks<Cursor> implementation
    */

    private static final String SELECTION = Projections.COLUMN_SYSTEM + "=?";

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader id=" + id);

        CursorLoader loader =  new CursorLoader(
                mContext,
                Uri.parse(Projections.CONTENT_URI),
                Projections.PROJECTION_SHORT,
                Projections.COLUMN_SYSTEM + "=?",
                new String[] {Integer.toString(mCurrentProjectionSystem)},
                Projections.DEFAULT_ORDER_BY
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        // Swap the new cursor in.  The framework will take
        // care of closing the old cursor once we return.
        mProjectionsAdapter.swapCursor(data);

        // Update the projections spinner selection.
        int position = 0;
        String code = mCurrentProjectionCode != null ?
                mCurrentProjectionCode : mRecents.get(mCurrentProjectionSystem);

        if (code != null) {

            // Find the code in the adapter data.
            data.moveToFirst();
            do {
                if (code.equals(data.getString(Projections.INDEX_CODE))) {
                    position = data.getPosition();
                    break;
                }
            } while (data.moveToNext());
        }

        // Update the projections spinner selection.
        mProjectionsSpinner.setSelection(position);

        // Update the current projection code.
        data.moveToPosition(position);
        mCurrentProjectionCode = data.getString(Projections.INDEX_CODE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mProjectionsAdapter.swapCursor(null);
    }

    /*
    * Cursor adapter for projections spinner.
    */

    private class ProjectionsAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public ProjectionsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.code))
                    .setText(cursor.getString(Projections.INDEX_CODE));
            ((TextView) view.findViewById(R.id.desc))
                    .setText(cursor.getString(Projections.INDEX_DESC));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.spinner_item_projection, parent, false);
            return v;
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            Log.d(TAG, "ProjectionsAdapter.swapCursor");
            return super.swapCursor(newCursor);
        }

        @Override
        protected void onContentChanged() {
            Log.d(TAG, "ProjectionsAdapter.onContentChanged");
            super.onContentChanged();
        }
    }
}
