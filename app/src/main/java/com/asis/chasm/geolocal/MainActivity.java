package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.asis.chasm.geolocal.Settings.Params;
import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends Activity implements
        PointsList.OnListFragmentInteractionListener,
        LocalReferencePref.OnPreferenceInteractionListener {

    private static final String TAG = "MainActivity";

    // Tags to identify the fragments
    public static final String FRAGMENT_POINTS_LIST = "list";
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

        // Check if the projections table has already been loaded.
        Bundle bundle = getContentResolver().call(Uri.parse(Projections.CONTENT_URI),
                PointsContract.CALL_GET_COUNT_METHOD, Projections.TABLE, null);

        if (bundle == null || bundle.getInt(PointsContract.CALL_GET_COUNT_RESULT_KEY) == 0) {
            loadProjectionsTable(PROJECTION_CONSTANTS_ASSET);
        }

        // Set defaults for the shared preferences.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Initialize the transform settings.
        Params.initialize(this);

        // Create the points list fragment.
        FragmentManager manager = getFragmentManager();
        Fragment fragment =fragment = new PointsList();
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

                    Fragment settings = new Settings();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack.
                    manager.beginTransaction()
                            .replace(R.id.container, settings, FRAGMENT_SETTINGS)
                            .addToBackStack(null)
                            .commit();
                }
                return true;

            case R.id.action_load_points:
                loadPointsFile();
                return true;
            case R.id.action_test:
                try { doTest(); }
                catch (IOException e) { Log.d(TAG, "IO exception: " + e); }
                catch (XmlPullParserException e) { Log.d(TAG, "Parser exception: " + e); }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doTest() throws IOException, XmlPullParserException {
        Toast.makeText(this, "MainActivity Test", Toast.LENGTH_SHORT).show();

        final String GPX_FILE = "Waypoints.gpx";

        InputStream stream = null;
        GpxParser gpxParser = new GpxParser();
        List<GpxParser.Waypoint> wpts = null;

        try {
            String path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/SPCS";
            Log.d(TAG, "path: " + path);

            File file = new File(path, GPX_FILE);
            stream = new FileInputStream(file);
            wpts = gpxParser.parse(stream);

            for (GpxParser.Waypoint wpt : wpts) {
                Log.d(TAG, wpt.toString());
            }

        } finally {
            if (stream != null) {
                stream.close();
            }
        }


    }

    // Respond to the action bar back button.
    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    /*
    * OnPreferenceInteractionListener - respond to events from the preference dialogs.
    *
    * onSelectLocalRefPoint - called form the LocalReferencePref to pick a point
    * from the points list and update the dialog values without actually persisting
    * the value to shared preferences.
    */

    @Override
    public void onSelectLocalRefPoint(Dialog dialog, View dialogView) {
        onPointPickedAction = POINT_SELECT_ACTION_LOCAL_REF;

        mPrefDialog = dialog;
        mPrefDialogView = dialogView;

        dialog.hide();

        // Create the points list fragment.
        Fragment fragment = new PointsList();
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

        Params p = Params.getParams();
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
                        .setText(String.format(p.getLocalUnitsFormat(),
                                c.getDouble(Points.INDEX_Y) * p.getUnitsFactor()));
                ((EditText) mPrefDialogView.findViewById(R.id.secondValue))
                        .setText(String.format(p.getLocalUnitsFormat(),
                                c.getDouble(Points.INDEX_X) * p.getUnitsFactor()));

                getFragmentManager().popBackStack();

                onPointPickedAction = POINT_SELECT_ACTION_DEFAULT;
                break;

            default:
                LocalPoint local = new LocalPoint(c.getDouble(Points.INDEX_X), c.getDouble(Points.INDEX_Y));
                GeoPoint geo = local.toGeo();
                GridPoint grid = geo.toGrid();

                Log.d(TAG, "grid ref n/e (" + p.getLocalUnitsAbbrev() + "): "
                        + String.format(p.getLocalUnitsFormat(), p.getGridRef().getY() * p.getUnitsFactor()) + ", "
                        + String.format(p.getLocalUnitsFormat(), p.getGridRef().getX() * p.getUnitsFactor()));
                Log.d(TAG, "grid ref theta (" + p.getRotationUnitsAbbrev() + "): "
                        + String.format(p.getRotationUnitsFormat(), p.getGridRef().getTheta()));
                Log.d(TAG, "point #" + c.getString(Points.INDEX_NAME) + ": " + c.getString(Points.INDEX_DESC));
                Log.d(TAG, "local n/e (" + p.getLocalUnitsAbbrev() + "): "
                        + String.format(p.getLocalUnitsFormat(), local.getY() * p.getUnitsFactor()) + ", "
                        + String.format(p.getLocalUnitsFormat(), local.getX() * p.getUnitsFactor()));
                Log.d(TAG, "grid n/e (" + p.getLocalUnitsAbbrev() + "): "
                        + String.format(p.getLocalUnitsFormat(), grid.getY() * p.getUnitsFactor()) + ", "
                        + String.format(p.getLocalUnitsFormat(), grid.getX() * p.getUnitsFactor()));
                Log.d(TAG, "geographic lat/lon (" + p.getGeographicUnitsAbbrev() + "): "
                        + String.format(p.getGeographicUnitsFormat(), geo.getLat()) + ", "
                        + String.format(p.getGeographicUnitsFormat(), geo.getLon()));
                break;
        }
    }

    /*
    * loadProjectionsTable - read projection data from a resource data file and
    * load them into the content provider's Projections table.
    */

    private void loadProjectionsTable(String filename) {

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

    /*
    * loadPointsFile - choose a file manager to select a local PNEZD points file
    * and load the points into the content provider's Points table.
    */

    // Activity result codes
    private final int RESULT_CODE_FILE_SELECT = 1;

    private void loadPointsFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File"), RESULT_CODE_FILE_SELECT);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {

            // Change the empty text message before starting the async task.
            ((PointsList) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_POINTS_LIST))
                    .setEmptyText("Loading points...");

            // Run an AsyncTask to read points into the content provider.
            new ReadPointsTask().execute(data.getData());
        }
    }

    // Async task to read the points, parse the data and load the Points table.
    private class ReadPointsTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... args) {

            Uri uri = args[0];
            Log.d(TAG, "ReadPointsTask points uri: " + uri);

            ContentResolver resolver = getContentResolver();
            ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();

            final Uri POINTS_URI = Uri.parse(Points.CONTENT_URI);

            // Delete any points already in the Points table.
            int deleted = getContentResolver().delete(POINTS_URI, null, null);
            Log.d(TAG, "Points deleted: " + deleted);

            BufferedReader reader = null;
            try {

                reader = new BufferedReader(
                        new InputStreamReader(resolver.openInputStream(uri)));

                String line;
                String[] parts;

                Params p = Params.getParams();
                while ((line = reader.readLine()) != null) {
                    // Ignore blank lines and comment lines which start with #
                    if (line.length() == 0 || line.startsWith("#")) {
                        continue;
                    }
                    parts = line.split(",", 5);
                    if (parts.length != 5) {
                        Log.d(TAG, "PNEZD (comma delimited) file format error: " + line);
                        continue;
                    }

                    Double y = Double.parseDouble(parts[1]) / p.getUnitsFactor();
                    Double x = Double.parseDouble(parts[2]) / p.getUnitsFactor();

                    ContentValues values = new ContentValues();

                    values.put(Points.COLUMN_NAME, parts[0]);
                    values.put(Points.COLUMN_Y, y);
                    values.put(Points.COLUMN_X, x);

                    // Skipping Z (elevation)
                    values.put(Points.COLUMN_DESC, parts[4]);
                    values.put(Points.COLUMN_TYPE, Points.TYPE_LOCAL);

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

            // Send the bulk insert to the content provider.
            int inserted = resolver.bulkInsert(POINTS_URI, valuesList.toArray(new ContentValues[0]));
            Log.d(TAG, "Points inserted: " + inserted);

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            // Restore the empty text.
            ((PointsList) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_POINTS_LIST))
                    .setEmptyText("No points.");
        }
    }

}
