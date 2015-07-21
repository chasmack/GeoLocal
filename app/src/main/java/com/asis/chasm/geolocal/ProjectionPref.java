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
import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;

/*
*  Projection preference.
*/

public class ProjectionPref extends DialogPreference implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "ProjectionPref";

    // Rotation saved in shared preferences as a formatted string in degrees.
    private static final String SHARED_PREFERENCES_COORD_FORMAT = "%.8f";

    private static final int DEFAULT_SYSTEM = Projections.SYSTEM_SPCS;
    private static final String DEFAULT_PROJECTION = "0401";

    // Current values for projection system and code.
    private int mCurrentProjectionSystem;
    private String mCurrentProjectionCode;
    public  String getValue() { return mCurrentProjectionCode; }

    // Set the selected projection item on load finished.
    private boolean mSetSelectedProjection;

    // Reference to the spinners.
    private Spinner mSystemsSpinner;
    private Spinner mProjectionsSpinner;

    // Reference to the fragment's loader manager.
    private LoaderManager mLoaderManager;

    // Reference to the application context.
    private Context mContext;

    // Array adapter for the coordinate systems spinner.
    private ArrayAdapter<CharSequence> mSystemsAdapter;

    // Cursor adapter for the projections spinner.
    private ProjectionsCursorAdapter mProjectionsAdapter;

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }

    public ProjectionPref(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "Constructor");

        mContext = context.getApplicationContext();

        setDialogLayoutResource(R.layout.dialog_projection);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

        mLoaderManager = ((Activity) context).getFragmentManager()
                .findFragmentByTag(MainActivity.FRAGMENT_SETTINGS)
                .getLoaderManager();
    }

    @Override
    protected View onCreateDialogView() {
        View view =  super.onCreateDialogView();

        // Reinitialize the current values.
        Params p = Params.getParams();
        mCurrentProjectionSystem = p.getProjectionSystem();
        mCurrentProjectionCode = p.getProjectionCode();

        // Get references to the two spinners.
        mSystemsSpinner = (Spinner) view.findViewById(R.id.systems_spinner);
        mProjectionsSpinner = (Spinner) view.findViewById(R.id.projections_spinner);

        // Create an array adapter for the systems spinner.
        mSystemsAdapter = ArrayAdapter.createFromResource(mContext,
                R.array.pref_projection_systems, android.R.layout.simple_spinner_item);

        // Set the layout to use when the list of choices appears.
        mSystemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Bind the array adapter to the systems spinner and set the listener.
        mSystemsSpinner.setAdapter(mSystemsAdapter);
        mSystemsSpinner.setSelection(mCurrentProjectionSystem);
        mSystemsSpinner.setOnItemSelectedListener(this);

        // Create a cursor adapter for the projections spinner.
        mProjectionsAdapter = new ProjectionsCursorAdapter(mContext, null, 0);

        // Connect the cursor adapter to the spinner and set the listener.
        mProjectionsSpinner.setAdapter(mProjectionsAdapter);
        mProjectionsSpinner.setOnItemSelectedListener(this);

        // Start the projections loader.
        mSetSelectedProjection = true;
        mLoaderManager.initLoader(1, null, this);

        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // When the user selects "OK", persist the new value
        if (positiveResult) {
            Log.d(TAG, "onDialogClosed mCurrentProjectionCode: " + mCurrentProjectionCode);
            persistString(mCurrentProjectionCode);
        }
    }

    /*
    * AdapterView.OnItemSelectedListener implementation.
    */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.systems_spinner:
                Log.d(TAG, "onItemSelected mSystemsSpinner pos=" + position + " id=" + id);
                mCurrentProjectionSystem = (int)id;
                mSetSelectedProjection = true;
                mLoaderManager.restartLoader(0, null, this);
                break;

            case R.id.projections_spinner:
                Log.d(TAG, "onItemSelected mProjectionsSpinner pos=" + position + " id=" + id);
                mCurrentProjectionCode = ((TextView)mProjectionsSpinner.getSelectedView().findViewById(R.id.code))
                        .getText().toString();
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

    private static final String[] PROJECTION = {
            Projections._ID,
            Projections.COLUMN_CODE,
            Projections.COLUMN_DESC
    };

    private static final String SELECTION = Projections.COLUMN_SYSTEM + "=?";

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onCreateLoader");

        CursorLoader loader =  new CursorLoader(
                mContext,
                Uri.parse(Projections.CONTENT_URI),
                PROJECTION,
                Projections.COLUMN_SYSTEM + "=?",
                new String[] {Integer.toString(mCurrentProjectionSystem)},
                Projections.COLUMN_CODE
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoadFinished");
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mProjectionsAdapter.swapCursor(data);

        if (mSetSelectedProjection) {

            // Find the item for the current projection.
            Cursor c = mContext.getContentResolver().query(
                    Uri.parse(Projections.CONTENT_URI),
                    new String[]{Projections._ID},
                    Projections.COLUMN_CODE + "=?",
                    new String[]{mCurrentProjectionCode},
                    null);
            if (c == null || !c.moveToFirst()) {
                throw new IllegalStateException("bad projection: " + mCurrentProjectionCode);
            }
            int id = c.getInt(0);
            c.close();

            int pos = 0;
            int count = mProjectionsSpinner.getCount();
            while (pos < count && mProjectionsSpinner.getItemIdAtPosition(pos) != id) {
                pos++;
            }
            if (pos < count) {
                Log.d(TAG, "current projection (count=" + count + " id=" + id + " pos=" + pos + ")");
            } else {
                Log.d(TAG, "can't find projection (count=" + count + " id=" + id + ")");
            }

            mProjectionsSpinner.setSelection(pos < count ? pos : 0);
            mSetSelectedProjection = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mProjectionsAdapter.swapCursor(null);
    }

    /*
    * Cursor adapter for projections spinner.
    */

    private class ProjectionsCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public ProjectionsCursorAdapter(Context context, Cursor c, int flags) {
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
            View v = mInflater.inflate(R.layout.list_item_projection, parent, false);
            return v;
        }

        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            Log.d(TAG, "ProjectionsCursorAdapter.onContentChanged");
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        Log.d(TAG, "onSetInitialValue persisted: " + restorePersistedValue);

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
}