package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Transforms;

/**
 * A simple {@link Fragment} subclass.
 */
public class PointsManagerFragment extends Fragment implements
        PointsListFragment.OnListFragmentInteractionListener {

    private static final String TAG = "ManagerFragment";


    public PointsManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        // No UI associated with this fragmant.
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.points_manager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_read_points:
                readPointsFile();
                return true;
            case R.id.action_manager_test:
                managerTest();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void managerTest() {
        Toast.makeText(getActivity(), "managerTest()", Toast.LENGTH_SHORT).show();

        TransformSettings settings = TransformSettings.getSettings();

        LocalPoint local = new LocalPoint(settings.getBaseX(), settings.getBaseY());
        Log.d(TAG, "local point (n/e): " + local.getY() + ", " + local.getX());

        GridPoint grid = local.toGrid();
        Log.d(TAG, "grid point (n/e): " + grid.getY() + ", " + grid.getX());

        GeoPoint geo = grid.toGeo();
        Log.d(TAG, "geo point (lat/lon): " + geo.getLat() + ", " + geo.getLon());

        grid = geo.toGrid();
        Log.d(TAG, "grid point (n/e): " + grid.getY() + ", " + grid.getX());

        local = grid.toLocal();
        Log.d(TAG, "local point (n/e): " + local.getY() + ", " + local.getX());

    }

    // Interaction from points list fragment onListItemClick
    public void onListFragmentInteraction(long id) {
        Log.d(TAG, "onListFragmentInteraction id: " + id);
    }

    // Activity result codes
    private final int RESULT_CODE_FILE_SELECT = 1;

    private void readPointsFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File"), RESULT_CODE_FILE_SELECT);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {

            // Change the empty text message..
            ListFragment fragment = (ListFragment) getFragmentManager()
                    .findFragmentByTag(MainActivity.FRAGMENT_POINTS_LIST);
            fragment.setEmptyText("Loading points...");

            // Delete any entries already in the points table
            int cnt = getActivity().getContentResolver().delete(Uri.parse(Points.CONTENT_URI),
                    Points.COLUMN_TYPE + "=" + Points.TYPE_LOCAL, null);
            Log.d(TAG, "onActivityResult points deleted: " + cnt);

            // Run an AsyncTask to read points into the content provider.
            new ReadLocalPointsTask().execute(data.getData());
        }
    }

    /*
    * Background task to update geographic coordinates in the points
    * database based on the current transform settings.
    */

    private class UpdateGeographicCoordinatesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "UpdateGeographicCoordinatesTask");

            ContentResolver resolver = getActivity().getContentResolver();
            ContentValues values = new ContentValues();

            final Uri POINTS_URI = Uri.parse(PointsContract.Points.CONTENT_URI);

            String[] projection = {Points._ID, Points.COLUMN_X, Points.COLUMN_Y};
            Cursor c = getActivity().getContentResolver().query(
                    POINTS_URI,
                    projection,
                    Points.COLUMN_TYPE + "=?",
                    new String[]{Integer.toString(Points.TYPE_LOCAL)},
                    null);

            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {

                    LocalPoint local = new LocalPoint(c.getDouble(1), c.getDouble(2));
                    GeoPoint geo = local.toGrid().toGeo();

                    values.clear();
                    values.put(Points.COLUMN_LAT, geo.getLat());
                    values.put(Points.COLUMN_LON, geo.getLon());

                    int cnt = resolver.update(POINTS_URI,
                            values, Points._ID + "=" + c.getLong(0), null);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    private class ReadLocalPointsTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... args) {

            Uri uri = args[0];
            Log.d(TAG, "ReadLocalPointsTask points uri: " + uri);

            ContentResolver resolver = getActivity().getContentResolver();
            ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();

            final Uri POINTS_URI = Uri.parse(PointsContract.Points.CONTENT_URI);

            BufferedReader reader = null;
            try {

                reader = new BufferedReader(
                        new InputStreamReader(resolver.openInputStream(uri)));

                String line;
                String[] parts;

                TransformSettings settings = TransformSettings.getSettings();
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

                    // Convert user units to system units (meters).
                    LocalPoint local = new LocalPoint(
                            Double.parseDouble(parts[2]) / settings.getUnitsFactor(),
                            Double.parseDouble(parts[1]) / settings.getUnitsFactor());

                    // Convert local coordinates to geographic.
                    GeoPoint geo = local.toGrid().toGeo();

                    ContentValues values = new ContentValues();

                    values.put(Points.COLUMN_NAME, parts[0]);
                    values.put(Points.COLUMN_Y, local.getY());
                    values.put(Points.COLUMN_X, local.getX());
                    // Skipping Z (elevation)
                    values.put(Points.COLUMN_DESC, parts[4]);
                    values.put(Points.COLUMN_TYPE, Points.TYPE_LOCAL);
                    values.put(Points.COLUMN_LAT, geo.getLat());
                    values.put(Points.COLUMN_LON, geo.getLon());

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
            int cnt = valuesList.size();
            resolver.bulkInsert(POINTS_URI, valuesList.toArray(new ContentValues[cnt]));

            Log.d(TAG, "ReadLocalPointsTask points inserted: " + cnt);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            // Restore the empty text.
            // Change the empty text message..
            ListFragment fragment = (ListFragment) getFragmentManager()
                    .findFragmentByTag(MainActivity.FRAGMENT_POINTS_LIST);
            fragment.setEmptyText("No points.");
        }
    }

}
