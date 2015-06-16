package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;
import com.asis.chasm.geolocal.PointsContract.Transforms;

public class MainActivity extends Activity implements
        PointsListFragment.OnFragmentInteractionListener {

    // Use for logging and debugging
    private static final String TAG = "MainActivity";

    /**
     * Fragment displaying the points list
     */
    private PointsListFragment mPointsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If we're being restored from a previous state, then
        // we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // Create a new Fragment to be placed in the activity layout
        mPointsListFragment = new PointsListFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        mPointsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'container' FrameLayout
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, (Fragment) mPointsListFragment);
        transaction.commit();

        // Start a background task to populate the Coordinate Systems table
        new PopulateProjectionsTableTask().execute("projections.txt");
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
                return true;

            case R.id.action_read_points:
                selectPointsFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Activity result codes
    private final int RESULT_CODE_FILE_SELECT = 1;

    private void selectPointsFile() {

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

            // Delete any entries already in the Coordinate Systems table
            int cnt = getContentResolver().delete(Uri.parse(Points.CONTENT_URI),
                    Points.COLUMN_TYPE + "=" + Points.POINT_TYPE_LOCAL, null);
            Log.d(TAG, "Local points deleted: " + cnt);

            getLoaderManager().restartLoader(0, null, mPointsListFragment);

            new ReadLocalPointsTask().execute(data.getData());
        }
    }

    private class ReadLocalPointsTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... params) {
            Uri uri = params[0];
            Log.d(TAG, "FILE_SELECT Uri: " + uri);

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getContentResolver().openInputStream(uri)));

                int cnt;
                String line;
                String[] parts;
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();

                final Uri POINTS_URI = Uri.parse(Points.CONTENT_URI);

                // Delete any entries already in the Coordinate Systems table
                cnt = resolver.delete(POINTS_URI,
                        Points.COLUMN_TYPE + " = " + Points.POINT_TYPE_LOCAL, null);
                Log.d(TAG, "Local points deleted: " + cnt);

                cnt = 0;
                while ((line = reader.readLine()) != null) {
                    // Ignore blank lines and comment lines which start with #
                    if (line.length() == 0 || line.startsWith("#")) {
                        continue;
                    }
                    parts = line.split(",", 5);
                    if (parts.length != 5) {
                        Log.d(TAG, "PNEZD file format error: " + line);
                        continue;
                    }
                    values.put(Points.COLUMN_NAME, parts[0]);
                    values.put(Points.COLUMN_Y, Double.parseDouble(parts[1]));
                    values.put(Points.COLUMN_X, Double.parseDouble(parts[2]));
                    // Skipping Z (elevation)
                    values.put(Points.COLUMN_DESC, parts[4]);
                    values.put(Points.COLUMN_TYPE, Points.POINT_TYPE_LOCAL);
                    resolver.insert(POINTS_URI, values);
                    cnt++;
                }
                Log.d(TAG, "Points loaded: " + cnt);

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
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            getLoaderManager().restartLoader(0, null, mPointsListFragment);
            Log.d(TAG, "ReadLocalPointsTask onPostExecute.");

        }
    }

    private class PopulateProjectionsTableTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open(params[0]), "UTF-8"));

                int cnt;
                String line;
                String[] parts;
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();

                final Uri PROJECTIONS_URI = Uri.parse(PointsContract.Projections.CONTENT_URI);

                // Delete any entries already in the Coordinate Systems table
                cnt = resolver.delete(PROJECTIONS_URI, null, null);
                Log.d(TAG, "Projections deleted: " + cnt);

                cnt = 0;
                READLINE:
                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("#"))
                        continue;       // comment lines start with #

                    parts = line.split(",", 11);
                    if (parts.length != 11) {
                        Log.d(TAG, "File format error: " + line);
                        continue;
                    }

                    // 0-CODE, 1-DESC, 2-TYPE, 3-PROJ, 4-P0, 5-M0, 6-X0, 7-Y0, 8-P1, 9-P2, 10-SF
                    values.put(Projections.COLUMN_CODE, parts[0]);
                    values.put(Projections.COLUMN_DESC, parts[1]);
                    switch (parts[2]) {
                        case "SPCS":
                            values.put(Projections.COLUMN_COORD_SYSTEM,
                                    Projections.COORD_SYSTEM_SPCS);
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
                    values.put(Projections.COLUMN_SF,
                            parts[10].isEmpty() ? 0 : Long.parseLong(parts[10]));

                    resolver.insert(PROJECTIONS_URI, values);
                    cnt++;
                }
                Log.d(TAG, "Projections loaded: " + cnt);

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
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.d(TAG, "PopulateProjectionsTableTask onPostExecute.");
        }
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

    @Override
    public void onPointsFragmentInteraction(int position) {

        Toast.makeText(this, "List item: " + position, Toast.LENGTH_SHORT).show();
    }
}
