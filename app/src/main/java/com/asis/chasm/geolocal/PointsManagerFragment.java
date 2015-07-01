package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
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

import com.asis.chasm.geolocal.PointsContract.Points;

/**
 * A simple {@link Fragment} subclass.
 */
public class PointsManagerFragment extends Fragment implements
        PointsListFragment.OnListFragmentInteractionListener {

    // Use for logging and debugging
    private static final String TAG = "PointsManagerFragment";

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
            case R.id.action_test_transform:
                testTransform();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void testTransform() {
        Toast.makeText(getActivity(), "Test Transform", Toast.LENGTH_SHORT).show();
    }

    // Interaction from points list fragment onListItemClick
    public void onListFragmentInteraction(long id) {
        Log.d(TAG, "onListFragmentInteraction id: " + id);
        Uri uri = Uri.parse(PointsContract.Points.CONTENT_URI)
                .buildUpon()
                .appendPath(Long.toString(id))
                .build();
        Cursor c = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (c != null && c.moveToFirst()) {

            Log.d(TAG, "point: " + c.getString(Points.INDEX_NAME)
                    + " - " + c.getString(Points.INDEX_DESC));
        }
        
        TransformParams params = new TransformParams(getActivity());
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
            int cnt = getActivity().getContentResolver().delete(Uri.parse(PointsContract.Points.CONTENT_URI),
                    PointsContract.Points.COLUMN_TYPE + "=" + PointsContract.Points.POINT_TYPE_LOCAL, null);
            Log.d(TAG, "onActivityResult points deleted: " + cnt);

            // Run an AsyncTask to read points into the content provider.
            new ReadLocalPointsTask().execute(data.getData());
        }
    }

    private class ReadLocalPointsTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... params) {

            Uri uri = params[0];
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

                    ContentValues values = new ContentValues();

                    values.put(PointsContract.Points.COLUMN_NAME, parts[0]);
                    values.put(PointsContract.Points.COLUMN_Y, Double.parseDouble(parts[1]));
                    values.put(PointsContract.Points.COLUMN_X, Double.parseDouble(parts[2]));
                    // Skipping Z (elevation)
                    values.put(PointsContract.Points.COLUMN_DESC, parts[4]);
                    values.put(PointsContract.Points.COLUMN_TYPE, PointsContract.Points.POINT_TYPE_LOCAL);
                    values.put(PointsContract.Points.COLUMN_LAT, 0.0);
                    values.put(PointsContract.Points.COLUMN_LON, 0.0);

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
