package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.asis.chasm.geolocal.Settings.Params;
import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.GeoPoints;
import com.asis.chasm.geolocal.PointsContract.Projections;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends Activity implements
        PointsList.OnListFragmentInteractionListener,
        LocalReferencePref.OnPreferenceInteractionListener {

    private static final String TAG = "MainActivity";

    // Tags to identify the fragments
    public static final String FRAGMENT_POINTS_LIST = "list";
    public static final String FRAGMENT_SETTINGS = "settings";

    // Loader ids.
    public static final int LOADER_ID_POINTS_LIST = 0;
    public static final int LOADER_ID_PREF_PROJECTIONS = 1;

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

    // Respond to the action bar back button.
    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_points_read:
                pointsRead();
                return true;
            case R.id.action_gpx_read:
                try { gpxRead(); }
                catch (IOException e) { Log.d(TAG, "IO exception: " + e); }
                catch (XmlPullParserException e) { Log.d(TAG, "Parser exception: " + e); }
                return true;
            case R.id.action_gpx_write:
                // Show the GPX write dialog.
                DialogFragment dialog = new gpxWriteDialogFragment();
                dialog.show(getFragmentManager(), "GpxWriteDialog");
                return true;
            case R.id.action_test:
                try { doTest(); }
                catch (IOException e) { Log.d(TAG, "IO exception: " + e); }
                catch (XmlPullParserException e) { Log.d(TAG, "Parser exception: " + e); }
                return true;
            case R.id.action_settings:
                FragmentManager manager = getFragmentManager();
                if (manager.findFragmentByTag(FRAGMENT_SETTINGS) == null) {
                    // Replace the fragment in the fragment container view with settings
                    // and add the transaction to the back stack.
                    manager.beginTransaction()
                            .replace(R.id.container, new Settings(), FRAGMENT_SETTINGS)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doTest() throws IOException, XmlPullParserException {
        Toast.makeText(this, "MainActivity Test", Toast.LENGTH_SHORT).show();

        final String OUTFILE = "test.txt";

        Writer writer = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outfile = new File(path, OUTFILE);
        try {
            writer = new FileWriter(outfile);
            writer.write("Hello world.\n");

        } finally {
            if (writer != null) writer.close();

            // Workaround to ensure new file is visible in Windows explorer.
            new SingleMediaScanner(this, outfile);
        }
    }

    /*
    * gpxRead - read the waypoints from a GPX file.
    */

    private void gpxRead() throws IOException, XmlPullParserException {
        Toast.makeText(this, "gpxRead", Toast.LENGTH_SHORT).show();

        final String INFILE = "Waypoints-01.gpx";

        // Read the GPX file extracting the waypoints into a list.
        List<GpxParser.Waypoint> wpts;
        InputStream stream = null;
        try {
            String path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/SPCS";
            Log.d(TAG, "path: " + path);

            File infile = new File(path, INFILE);
            stream = new FileInputStream(infile);
            wpts = new GpxParser().parse(stream);

        } finally {
            if (stream != null) stream.close();
        }

        // Write list of waypoints to the database.
        ContentResolver resolver = getContentResolver();
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();

        final Uri GEOPOINTS_URI = Uri.parse(GeoPoints.CONTENT_URI);

        // Delete any points already in the Points table.
        int deleted = getContentResolver().delete(GEOPOINTS_URI, null, null);
        Log.d(TAG, "GeoPoints deleted: " + deleted);

        for (GpxParser.Waypoint wpt : wpts) {
            Log.d(TAG, wpt.toString());

            ContentValues values = new ContentValues();

            values.put(GeoPoints.COLUMN_LAT, wpt.lat);
            values.put(GeoPoints.COLUMN_LON, wpt.lon);
            values.put(GeoPoints.COLUMN_TIME, wpt.time);
            values.put(GeoPoints.COLUMN_NAME, wpt.name);
            values.put(GeoPoints.COLUMN_CMT,  wpt.cmt);
            values.put(GeoPoints.COLUMN_DESC, wpt.desc);
            values.put(GeoPoints.COLUMN_SYMBOL, wpt.symbol);
            values.put(GeoPoints.COLUMN_SAMPLES, wpt.samples);

            valuesList.add(values);
        }

        // Send the bulk insert to the content provider.
        int inserted = resolver.bulkInsert(GEOPOINTS_URI, valuesList.toArray(new ContentValues[0]));
        Log.d(TAG, "GeoPoints inserted: " + inserted);
    }

    /*
    * gpxWrite - write local points to a gpx file
    */

    // A dialog to select a filename and call gpxWrite
    public static class gpxWriteDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            builder.setView(inflater.inflate(R.layout.dialog_gpx_write, null))
                    .setMessage(getString(R.string.gpx_write_message))

                    // Add action buttons
                    .setPositiveButton(R.string.gpx_write_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Get the filename from the edit text.
                            String filename = ((EditText) getDialog().findViewById(R.id.gpx_write_filename))
                                    .getText().toString().trim();

                            // Replace any terminal dots.
                            while (filename.endsWith(".")) {
                                filename = filename.substring(0, filename.length() - 1);
                            }
                            if (!filename.isEmpty()) {
                                if (!filename.matches("(?i).+\\.gpx")) {
                                    // Add the .gpx extension.
                                    filename += ".gpx";
                                }
                                String path = Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS).toString();
                                File file = new File(path, filename);

                                // Make sure the path exists and is writable.
                                File parent = file.getParentFile();
                                if (parent.exists()) {

                                    Log.d(TAG, "gpxWrite file: " + file.toString());
                                    try {
                                        gpxWrite(getActivity(), file);
                                    } catch (IOException e) {
                                        Log.d(TAG, "IO exception: " + e);
                                        Toast.makeText(getActivity(), "An error occurred:\n" + e,
                                                Toast.LENGTH_SHORT).show();
                                    } catch (XmlPullParserException e) {
                                        Log.d(TAG, "Parser exception: " + e);
                                        Toast.makeText(getActivity(), "An error occurred:\n" + e,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Log.d(TAG, "gpxWrite can't create dir: " + file.getParent());
                                    Toast.makeText(getActivity(), "Cannot create directory " + file.getParent(),
                                            Toast.LENGTH_LONG).show();
                                    getDialog().cancel();
                                }

                            } else {
                                Log.d(TAG, "gpxWrite filename is empty");
                                Toast.makeText(getActivity(), "File name is empty.",
                                        Toast.LENGTH_LONG).show();
                                getDialog().cancel();
                            }
                        }
                    })
                    .setNegativeButton(R.string.gpx_write_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            gpxWriteDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    private static void gpxWrite(Context context, File file)
            throws IOException, XmlPullParserException {

        // Get the point data.
        Uri uri = Uri.parse(PointsContract.Points.CONTENT_URI);
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);

        List<GpxParser.Waypoint> wpts = new ArrayList<GpxParser.Waypoint>();
        Params p = Params.getParams();

        int cnt = 0;
        c.moveToFirst();
        do {
            cnt++;
            GeoPoint geo = new LocalPoint(
                    c.getDouble(Points.INDEX_X), c.getDouble(Points.INDEX_Y))
                    .toGeo();

            wpts.add(new GpxParser.Waypoint(
                    String.format(p.getGeographicUnitsFormat(), geo.getLat()),
                    String.format(p.getGeographicUnitsFormat(), geo.getLon()),
                    null,
                    c.getString(Points.INDEX_NAME),
                    c.getString(Points.INDEX_DESC),
                    c.getString(Points.INDEX_DESC),
                    null,
                    0));

        } while (c.moveToNext());
        c.close();

        Writer writer = null;
        try {
            writer = new FileWriter(file);
            new GpxWriter().write(writer, wpts);

        } finally {
            if (writer != null) writer.close();

            // Workaround to ensure new file is visible in Windows explorer.
            new SingleMediaScanner(context, file);
        }
    }

    /*
    * pointsRead - choose a file manager to select a local PNEZD points file
    * and load the points into the content provider's Points table.
    */

    // Activity result codes
    private final int RESULT_CODE_FILE_SELECT = 1;

    private void pointsRead() {

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
        c.close();
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
                        values.put(Projections.COLUMN_SYSTEM, Projections.SYSTEM_SPCS);
                        break;
                    case "UTM":
                        values.put(Projections.COLUMN_SYSTEM, Projections.SYSTEM_UTM);
                        break;
                    case "USER":
                        values.put(Projections.COLUMN_SYSTEM, Projections.SYSTEM_USER);
                        break;
                    default:
                        Log.d(TAG, "Invalid projection TYPE: " + line);
                        continue READLINE;
                }
                switch (parts[3]) {
                    case "L":
                        values.put(Projections.COLUMN_TYPE, Projections.TYPE_LC);
                        break;
                    case "T":
                        values.put(Projections.COLUMN_TYPE, Projections.TYPE_TM);
                        break;
                    case "O":
                        values.put(Projections.COLUMN_TYPE, Projections.TYPE_OM);
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
    * SingleMediaScanner - workaround for issues with new file visability
    * using USB connection with Windows explorer.
    */

    private static class SingleMediaScanner implements
            MediaScannerConnection.MediaScannerConnectionClient
    {
        private MediaScannerConnection mScanner;
        private File mFile;

        SingleMediaScanner(Context context, File file) {
            mFile = file;
            mScanner = new MediaScannerConnection(context, this);
            mScanner.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            mScanner.scanFile(mFile.toString(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mScanner.disconnect();
        }
    }
}
