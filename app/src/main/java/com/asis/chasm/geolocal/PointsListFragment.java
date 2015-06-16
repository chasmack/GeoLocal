package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.dummy.DummyContent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PointsListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Use for logging and debugging
    private static final String TAG = "PointsListFragment";

    // Hook back into main activity.
    private OnFragmentInteractionListener mListener;

    // This is the Adapter being used to display the list's data.
    PointsCursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PointsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No points.");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new PointsCursorAdapter(getActivity(), null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.points_list, menu);
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
                selectPointsFile();
                return true;
            case R.id.action_show_local:
                mAdapter.showLocalCoordinates();
                return true;
            case R.id.action_show_geographic:
                mAdapter.showGeographicCoordinates();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("FragmentComplexList", "Item clicked: " + id);
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onPointsFragmentInteraction(position);
        }
    }

    /*
    * Custom cursor adapter for points list items
    */

    static class PointsCursorAdapter extends CursorAdapter {

        private static final int DECIMAL_PLACES_GEOGRAPHIC = 6;
        private static final int DECIMAL_PLACES_LOCAL = 3;

        private String formatLocal;
        private String formatGeographic;

        private boolean showGeographic = false;

        private LayoutInflater mInflater;

        public PointsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);

            formatLocal = "%." + DECIMAL_PLACES_LOCAL + "f / %." + DECIMAL_PLACES_LOCAL + "f";
            formatGeographic = "%." + DECIMAL_PLACES_GEOGRAPHIC + "f / %." + DECIMAL_PLACES_GEOGRAPHIC + "f";

            mInflater = LayoutInflater.from(context);
        }

        public void showLocalCoordinates() {
            showGeographic = false;
            notifyDataSetChanged();
        }

        public void showGeographicCoordinates() {
            showGeographic = true;
            notifyDataSetChanged();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView v;
            v = (TextView) view.findViewById(R.id.name);
            v.setText(cursor.getString(Points.INDEX_NAME));
            v = (TextView) view.findViewById(R.id.desc);
            v.setText(cursor.getString(Points.INDEX_DESC));
            if (showGeographic) {
                v = (TextView) view.findViewById(R.id.coords);
                v.setText(String.format(formatGeographic, cursor.getFloat(Points.INDEX_LAT), cursor.getFloat(Points.INDEX_LON)));
                v = (TextView) view.findViewById(R.id.coord_type);
                v.setText("Lat/Lon:");
            } else {
                v = (TextView) view.findViewById(R.id.coords);
                v.setText(String.format(formatLocal, cursor.getFloat(Points.INDEX_Y), cursor.getFloat(Points.INDEX_X)));
                v = (TextView) view.findViewById(R.id.coord_type);
                v.setText("N/E:");
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.list_item_point, parent, false);
            return v;
        }

        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            Log.d(TAG, "PointsCursorAdapter.onContentChanged");
        }
    }

    /*
    * LoaderCallbacks interface implementation
    */

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),          // Parent activity context
                Uri.parse(Points.CONTENT_URI),
                Points.PROJECTION,      // Projection to return
                null,                   // No selection clause
                null,                    // No selection arguments
                null                    // Default sort order
        );
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
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
            Toast.makeText(getActivity(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {

            // Delete any entries already in the Coordinate Systems table
            int cnt = getActivity().getContentResolver().delete(Uri.parse(Points.CONTENT_URI),
                    Points.COLUMN_TYPE + "=" + Points.POINT_TYPE_LOCAL, null);
            Log.d(TAG, "Local points deleted: " + cnt);

            setEmptyText("Loading points...");

            getLoaderManager().restartLoader(0, null, this);
            mAdapter.notifyDataSetChanged();

            new ReadLocalPointsTask(this).execute(data.getData());
        }
    }

    private class ReadLocalPointsTask extends AsyncTask<Uri, Void, Integer> {

        LoaderManager.LoaderCallbacks<?> mLoaderCallbacksManager;

        public ReadLocalPointsTask(LoaderManager.LoaderCallbacks<?> callbacksManager) {
            super();
            mLoaderCallbacksManager = callbacksManager;
        }

        @Override
        protected Integer doInBackground(Uri... params) {
            Uri uri = params[0];
            Log.d(TAG, "FILE_SELECT Uri: " + uri);

            int cnt = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getActivity().getContentResolver().openInputStream(uri)));

                String line;
                String[] parts;
                ContentResolver resolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues();

                final Uri POINTS_URI = Uri.parse(Points.CONTENT_URI);

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
                    values.put(Points.COLUMN_LAT, 0.0);
                    values.put(Points.COLUMN_LON, 0.0);

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
            return cnt;
        }

        @Override
        protected void onPostExecute(Integer cnt) {
            // super.onPostExecute(cnt);
            setEmptyText("No points.");

            getLoaderManager().restartLoader(0, null, mLoaderCallbacksManager);
            mAdapter.notifyDataSetChanged();

            Log.d(TAG, "ReadLocalPointsTask onPostExecute cnt: " + cnt);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onPointsFragmentInteraction(int position);
    }

}
