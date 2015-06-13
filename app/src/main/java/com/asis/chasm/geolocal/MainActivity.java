package com.asis.chasm.geolocal;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        LocalPointsListFragment.OnFragmentInteractionListener,
        GeoPointsListFragment.OnFragmentInteractionListener {

    // Use for logging and debugging
    private static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Fragment displaying the local points list
     */
    private LocalPointsListFragment mLocalPointsListFragment;

    /**
     * Fragment displaying the geographic point list
     */
    private GeoPointsListFragment mGeoPointsListFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Populate Coordinate Systems table
        populateProjectionsTable("projections.txt");

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position + 1) {
            case 1:
                mTitle = getString(R.string.title_section1);
                if (mLocalPointsListFragment == null) {
                    mLocalPointsListFragment = new LocalPointsListFragment();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mLocalPointsListFragment)
                        .commit();
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                if (mGeoPointsListFragment == null) {
                    mGeoPointsListFragment = new GeoPointsListFragment();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mGeoPointsListFragment)
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
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
            readPointsFile(data.getData());
        }
    }

    private void readPointsFile(Uri uri) {

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

            final Uri POINTS_URI = Uri.parse(PointsContract.Points.CONTENT_URI);

            // Delete any entries already in the Coordinate Systems table
            cnt = resolver.delete(POINTS_URI,
                    PointsContract.Points.COLUMN_TYPE + " = " + PointsContract.TYPE_LOCAL, null);
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
                values.put(PointsContract.Points.COLUMN_NAME, parts[0]);
                values.put(PointsContract.Points.COLUMN_Y, Double.parseDouble(parts[1]));
                values.put(PointsContract.Points.COLUMN_X, Double.parseDouble(parts[2]));
                // Skipping Z (elevation)
                values.put(PointsContract.Points.COLUMN_DESC, parts[4]);
                values.put(PointsContract.Points.COLUMN_TYPE, PointsContract.TYPE_LOCAL);
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
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_main, container, false);
            TextView text = (TextView) root.findViewById(R.id.section_label);
            if (text != null) {
                text.setText("Section number: " + getArguments().getInt(ARG_SECTION_NUMBER));
            }
            return root;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private void populateProjectionsTable(String datafile) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(datafile), "UTF-8"));

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
                values.put(PointsContract.Projections.COLUMN_CODE, parts[0]);
                values.put(PointsContract.Projections.COLUMN_DESC, parts[1]);
                switch (parts[2]) {
                    case "SPCS":
                        values.put(PointsContract.Projections.COLUMN_COORD_SYSTEM,
                                PointsContract.COORD_SYSTEM_SPCS);
                        break;
                    case "UTM":
                        values.put(PointsContract.Projections.COLUMN_COORD_SYSTEM,
                                PointsContract.COORD_SYSTEM_UTM);
                        break;
                    case "USER":
                        values.put(PointsContract.Projections.COLUMN_COORD_SYSTEM,
                                PointsContract.COORD_SYSTEM_USER);
                        break;
                    default:
                        Log.d(TAG, "Invalid projection TYPE: " + line);
                        continue READLINE;
                }
                switch (parts[3]) {
                    case "L":
                        values.put(PointsContract.Projections.COLUMN_PROJECTION,
                                PointsContract.PROJECTION_LC);
                        break;
                    case "T":
                        values.put(PointsContract.Projections.COLUMN_PROJECTION,
                                PointsContract.PROJECTION_TM);
                        break;
                    case "O":
                        values.put(PointsContract.Projections.COLUMN_PROJECTION,
                                PointsContract.PROJECTION_OM);
                        break;
                    default:
                        Log.d(TAG, "Invalid projection PROJ: " + line);
                        continue READLINE;
                }
                values.put(PointsContract.Projections.COLUMN_P0, parseDegMin(parts[4]));
                values.put(PointsContract.Projections.COLUMN_M0, parseDegMin(parts[5]));
                values.put(PointsContract.Projections.COLUMN_X0, Double.parseDouble(parts[6]));
                values.put(PointsContract.Projections.COLUMN_Y0, Double.parseDouble(parts[7]));
                values.put(PointsContract.Projections.COLUMN_P1, parseDegMin(parts[8]));
                values.put(PointsContract.Projections.COLUMN_P2, parseDegMin(parts[9]));
                values.put(PointsContract.Projections.COLUMN_SF,
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

    public void onLocalPointsFragmentInteraction(String id) {

        Uri uri;
        switch (id) {
            case "1":
                uri = Uri.parse(PointsContract.Points.CONTENT_URI);
                break;
            case "2":
                uri = Uri.parse(PointsContract.Projections.CONTENT_URI);
                break;
            case "3":
            default:
                uri = Uri.parse(PointsContract.Transforms.CONTENT_URI);
                break;

        }
        Log.d(TAG, "Calling ContentResolver.getType: " + uri);

        String type = getContentResolver().getType(uri);

        Toast.makeText(this, "Content type: " + type, Toast.LENGTH_LONG).show();
    }

    public void onGeoPointsFragmentInteraction(String id) {
        Uri uri;
        switch (id) {
            case "1":
                uri = Uri.parse(PointsContract.Points.CONTENT_URI + "/1");
                break;
            case "2":
                uri = Uri.parse(PointsContract.Projections.CONTENT_URI + "/1");
                break;
            case "3":
            default:
                uri = Uri.parse(PointsContract.Transforms.CONTENT_URI + "/1");
                break;

        }
        Log.d(TAG, "Calling ContentResolver.getType: " + uri);

        String type = getContentResolver().getType(uri);

        Toast.makeText(this, "Content type: " + type, Toast.LENGTH_LONG).show();

    }

}
