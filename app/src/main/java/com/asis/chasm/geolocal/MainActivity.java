package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;

public class MainActivity extends Activity implements
        PointsListFragment.OnListFragmentInteractionListener,
        LocalRefPreference.OnPreferenceInteractionListener {

    // Use for logging and debugging
    private static final String TAG = "MainActivity";

    // Preference interaction listener implementation.
    @Override
    public void onSelectLocalRefPoint(Dialog dialog, View dialogView) {
        onPointPickedAction = POINT_SELECT_ACTION_LOCAL_REF;

        mPrefDialog = dialog;
        mPrefDialogView = dialogView;

        dialog.hide();

        // Create the points list fragment.
        Fragment fragment = new PointsListFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, null)
                .addToBackStack(null)
                .commit();

        // Finish replacing the fragment and register a back stack change listener.
        getFragmentManager().executePendingTransactions();
        getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                // The back stack listener takes care of un-hiding the local reference
                // dialog if either the user picks a point and the list fragment
                // interaction listener pops the preferences fragment off the back
                // stack or the user hits back from the points list without picking
                // a point.

                mPrefDialog.show();

                // Unregister the back stack listener and clean up the references.
                getFragmentManager().removeOnBackStackChangedListener(this);
                mPrefDialog = null;
                mPrefDialogView = null;
            }
        });
    }

    // What to do with the point picked from the points list.
    private final int POINT_SELECT_ACTION_DEFAULT = 0;
    private final int POINT_SELECT_ACTION_LOCAL_REF = 1;

    private int onPointPickedAction = POINT_SELECT_ACTION_DEFAULT;
    private Dialog mPrefDialog = null;
    private View mPrefDialogView = null;

    // Interaction from points list fragment onListItemClick
    public void onListFragmentInteraction(long id) {
        Log.d(TAG, "onListFragmentInteraction id: " + id);

        // Get the point data.
        Uri uri = Uri.parse(PointsContract.Points.CONTENT_URI)
                .buildUpon().appendPath(Long.toString(id)).build();
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        if (!c.moveToFirst()) {
            throw new IllegalArgumentException("Invalid point ID: " + id);
        }

        TransformSettings s = TransformSettings.getSettings();
        switch (onPointPickedAction) {
            case POINT_SELECT_ACTION_LOCAL_REF:

                /*
                * The user picked "Select a point" from the local reference point preference.
                * MainActivity#onSelectLocalRefPoint hides the preference dialog and swaps in
                * the points list fragment pushing the preference fragment onto the back stack.
                *
                * Now we need to update the preference values and pop the preference fragment
                * off of the back stack.  The back stack listener takes care of showing the
                * preference dialog.
                */

                ((EditText) mPrefDialogView.findViewById(R.id.firstValue))
                        .setText(String.format(s.getLocalUnitsFormat(),
                                c.getDouble(Points.INDEX_Y) * s.getUnitsFactor()));
                ((EditText) mPrefDialogView.findViewById(R.id.secondValue))
                        .setText(String.format(s.getLocalUnitsFormat(),
                                c.getDouble(Points.INDEX_X) * s.getUnitsFactor()));

                getFragmentManager().popBackStack();

                onPointPickedAction = POINT_SELECT_ACTION_DEFAULT;
                break;

            default:
                LocalPt local = new LocalPt(c.getDouble(Points.INDEX_X), c.getDouble(Points.INDEX_Y));
                GeoPt geo = local.toGeo();
                GridPt grid = geo.toGrid();

                Log.d(TAG, "grid ref n/e (" + s.getLocalUnitsAbbrev() + "): "
                        + String.format(s.getLocalUnitsFormat(), s.getGridRef().getY() * s.getUnitsFactor()) + ", "
                        + String.format(s.getLocalUnitsFormat(), s.getGridRef().getX() * s.getUnitsFactor()));
                Log.d(TAG, "grid ref theta (" + s.getRotationUnitsAbbrev() + "): "
                        + String.format(s.getRotationUnitsFormat(), s.getGridRef().getTheta()));
                Log.d(TAG, "point #" + c.getString(Points.INDEX_NAME) + ": " + c.getString(Points.INDEX_DESC));
                Log.d(TAG, "local n/e (" + s.getLocalUnitsAbbrev() + "): "
                        + String.format(s.getLocalUnitsFormat(), local.getY() * s.getUnitsFactor()) + ", "
                        + String.format(s.getLocalUnitsFormat(), local.getX() * s.getUnitsFactor()));
                Log.d(TAG, "grid n/e (" + s.getLocalUnitsAbbrev() + "): "
                        + String.format(s.getLocalUnitsFormat(), grid.getY() * s.getUnitsFactor()) + ", "
                        + String.format(s.getLocalUnitsFormat(), grid.getX() * s.getUnitsFactor()));
                Log.d(TAG, "geographic lat/lon (" + s.getGeographicUnitsAbbrev() + "): "
                        + String.format(s.getGeographicUnitsFormat(), geo.getLat()) + ", "
                        + String.format(s.getGeographicUnitsFormat(), geo.getLon()));
                break;
        }
    }

    // Tags to identify the fragments
    public static final String FRAGMENT_POINTS_LIST = "list";
    public static final String FRAGMENT_POINTS_MANAGER = "manager";
    public static final String FRAGMENT_SETTINGS = "settings";

    // XML asset file with projection constants.
    private static final String PROJECTION_CONSTANTS_ASSET = "projections.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // If we're being restored from a previous state, then
        // we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // TODO: Don't reload projections every time the app is started.

        // Load the Projections table
        loadProjections(PROJECTION_CONSTANTS_ASSET);

        // Set defaults for the shared preferences.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Initialize the transform settings.
        TransformSettings.initialize(this);

        // Create the Non-UI points manager fragment.
        FragmentManager manager = getFragmentManager();
        Fragment fragment = new PointsManagerFragment();
        manager.beginTransaction()
               .add((Fragment) fragment, FRAGMENT_POINTS_MANAGER).commit();

       // Create the points list fragment.
        fragment = new PointsListFragment();
        manager.beginTransaction()
                .add(R.id.container, fragment, FRAGMENT_POINTS_LIST).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                FragmentManager manager = getFragmentManager();
                if (manager.findFragmentByTag(FRAGMENT_SETTINGS) == null) {

                    Fragment settings = new TransformSettingsFragment();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack.
                    manager.beginTransaction()
                            .replace(R.id.container, settings, FRAGMENT_SETTINGS)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    /*
    * Read projection data from a resource data file and
    * write it to the content provider's Projections table.
    */

    private void loadProjections(String filename) {

        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        ContentResolver resolver = getContentResolver();

        final Uri PROJECTIONS_URI = Uri.parse(PointsContract.Projections.CONTENT_URI);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(filename), "UTF-8"));

            String line;
            String[] parts;

            // Delete any entries already in the Coordinate Systems table
            int cnt = resolver.delete(PROJECTIONS_URI, null, null);
            Log.d(TAG, "Projections deleted: " + cnt);

            READLINE:
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#"))
                    continue;       // comment lines start with #

                parts = line.split(",", 11);
                if (parts.length != 11) {
                    Log.d(TAG, "File format error: " + line);
                    continue;
                }

                ContentValues values = new ContentValues();

                // 0-CODE, 1-DESC, 2-TYPE, 3-PROJ, 4-P0, 5-M0, 6-X0, 7-Y0, 8-P1, 9-P2, 10-SF
                values.put(Projections.COLUMN_CODE, parts[0]);
                values.put(Projections.COLUMN_DESC, parts[1]);
                switch (parts[2]) {
                    case "SPCS":
                        values.put(Projections.COLUMN_COORD_SYSTEM, Projections.COORD_SYSTEM_SPCS);
                        break;
                    case "UTM":
                        values.put(Projections.COLUMN_COORD_SYSTEM, Projections.COORD_SYSTEM_UTM);
                        break;
                    case "USER":
                        values.put(Projections.COLUMN_COORD_SYSTEM, Projections.COORD_SYSTEM_USER);
                        break;
                    default:
                        Log.d(TAG, "Invalid projection TYPE: " + line);
                        continue READLINE;
                }
                switch (parts[3]) {
                    case "L":
                        values.put(Projections.COLUMN_PROJECTION, Projections.PROJECTION_LC);
                        break;
                    case "T":
                        values.put(Projections.COLUMN_PROJECTION, Projections.PROJECTION_TM);
                        break;
                    case "O":
                        values.put(Projections.COLUMN_PROJECTION, Projections.PROJECTION_OM);
                        break;
                    default:
                        Log.d(TAG, "Invalid projection PROJ: " + line);
                        continue READLINE;
                }
                values.put(Projections.COLUMN_P0, parseDegMin(parts[4]));
                values.put(Projections.COLUMN_M0, parseDegMin(parts[5]));
                values.put(Projections.COLUMN_X0, Double.parseDouble(parts[6]));
                values.put(Projections.COLUMN_Y0, Double.parseDouble(parts[7]));
                values.put(Projections.COLUMN_P1, parseDegMin(parts[8]));
                values.put(Projections.COLUMN_P2, parseDegMin(parts[9]));

                // Convert integer scale factor to K0 = 1 - 1/SF
                values.put(Projections.COLUMN_K0,
                        parts[10].isEmpty() ? 0.0 :
                                1.0 - 1.0 / Long.parseLong(parts[10]));

                valuesList.add(values);
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }

        // Perform the bulk insert
        int cnt = resolver.bulkInsert(PROJECTIONS_URI, valuesList.toArray(new ContentValues[0]));
        Log.d(TAG, "Projections loaded: " + cnt);
    }

    private double parseDegMin(String dmString) {
        double val;
        String[] parts;
        boolean neg = false;

        if (dmString == null) {
            throw new IllegalArgumentException("Null DEG-MIN string.");
        }
        if (dmString.isEmpty()) {
            return 0.0;                 // empty string is valid
        }
        if (dmString.startsWith("-")) {
            neg = true;
            parts = dmString.substring(1).split("-");
        } else {
            parts = dmString.split("-");
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid DEG-MIN string: " + dmString);
        }
        val = Integer.parseInt(parts[0]) + Integer.parseInt(parts[1])/60.0;

        return neg ? -1.0 * val : val;
    }
}
