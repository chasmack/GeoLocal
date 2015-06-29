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
import java.util.ArrayList;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;
import com.asis.chasm.geolocal.PointsContract.Transforms;

public class MainActivity extends Activity implements
        PointsListFragment.OnFragmentInteractionListener,
        PointsManagerFragment.OnFragmentInteractionListener {

    // Use for logging and debugging
    private static final String TAG = "MainActivity";

    // Tags to identify the fragments
    public static final String FRAGMENT_POINTS_LIST = "list";
    public static final String FRAGMENT_POINTS_MANAGER = "manager";
    public static final String FRAGMENT_SETTINGS = "settings";

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

        // Add the points list fragment to the 'container' FrameLayout
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .add(R.id.container, (Fragment) mPointsListFragment, FRAGMENT_POINTS_LIST)
                .commit();

        // Create the Non-UI points manager fragment
        manager.beginTransaction()
                .add((Fragment) new PointsManagerFragment(), FRAGMENT_POINTS_MANAGER)
                .commit();

        // Start a background task to load the Projections table
        loadProjections("projections.txt");
        // new PopulateProjectionsTableTask().execute("projections.txt");
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadProjections(String filename) {

        /*
        * Background task to read projection data from a resource data file
        * and write it to the content provider's Projections table
        */

        final String fn = filename;

        new Thread(new Runnable() {

            @Override
            public void run() {

                ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
                ContentResolver resolver = getContentResolver();

                final Uri PROJECTIONS_URI = Uri.parse(PointsContract.Projections.CONTENT_URI);

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(getAssets().open(fn), "UTF-8"));

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
                int cnt = valuesList.size();
                resolver.bulkInsert(PROJECTIONS_URI, valuesList.toArray(new ContentValues[cnt]));

                Log.d(TAG, "Projections loaded: " + cnt);
            }
        }).start();
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

    public void onPointsFragmentInteraction(int position) {
        Toast.makeText(this, "List item: " + position, Toast.LENGTH_SHORT).show();
    }

    public void onPointsManagerFragmentInteraction(int code) {
        Toast.makeText(this, "List item: " + code, Toast.LENGTH_SHORT).show();
    }
}
