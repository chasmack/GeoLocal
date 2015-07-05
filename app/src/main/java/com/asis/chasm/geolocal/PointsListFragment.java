package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Transforms;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PointsListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // Use for logging and debugging
    private static final String TAG = "ListFragment";

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
    public interface OnListFragmentInteractionListener {
        public void onListFragmentInteraction(long position);
    }

    // Hook back into main activity.
    private OnListFragmentInteractionListener mListener;

    // This is the Adapter being used to display the list's data.
    PointsCursorAdapter mAdapter;

    // Display units factor. Internal coordinates are metric.
    // The units factor is used to convert meters to display units.
    private static double mDisplayUnitsFactor = 1.0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PointsListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        try {
            Fragment manager = getFragmentManager()
                    .findFragmentByTag(MainActivity.FRAGMENT_POINTS_MANAGER);
            if (manager != null) {
                mListener = (OnListFragmentInteractionListener) manager;
            } else {
                Log.d(TAG, "Can't find FRAGMENT_POINTS_MANAGER.");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        // Inflate our custom layout for the list fragment
        return inflater.inflate(R.layout.fragment_points_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        // Set onClickListeners for the Local/Grid/Geographic buttons
        RadioButton radio = (RadioButton) getActivity().findViewById(R.id.radio_local);
        radio.setChecked(true);
        radio.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAdapter.showLocalCoordinates();
            }
        });
        getActivity().findViewById(R.id.radio_geographic).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAdapter.showGeographicCoordinates();
            }
        });

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No points.");

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new PointsCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get display units from the points manager.
        mDisplayUnitsFactor = ((PointsManagerFragment) getFragmentManager()
                .findFragmentByTag(MainActivity.FRAGMENT_POINTS_MANAGER)).getDisplayUnits();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
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
            case R.id.action_test:
                test();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void test() {
        Toast.makeText(getActivity(), "Menu Item Test.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(TAG, "Item clicked: " + id);
        if (null != mListener) {
            // Notify the points manager active callbacks interface
            // that an item has been selected.
            mListener.onListFragmentInteraction(id);
        }
    }

    /*
    * Need to override setEmptyText when using a custom layout for ListFragment.
    */

    @Override
    public void setEmptyText(CharSequence text) {
        TextView view = (TextView) getActivity().findViewById(android.R.id.empty);
        view.setText(text);
    }

    /*
    * Custom cursor adapter for points list items
    */

    static class PointsCursorAdapter extends CursorAdapter {

        private static final int DECIMAL_PLACES_GEOGRAPHIC = 6;
        private static final int DECIMAL_PLACES_LOCAL = 2;

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

        public void showGridCoordinates() {
            showGeographic = true;
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
                v.setText(String.format(formatGeographic,
                        cursor.getDouble(Points.INDEX_LAT),
                        cursor.getDouble(Points.INDEX_LON)));
                v = (TextView) view.findViewById(R.id.coord_type);
                v.setText("Lat/Lon:");
            } else {
                v = (TextView) view.findViewById(R.id.coords);

                // TODO: Hook up a display units setting.
                v.setText(String.format(formatLocal,
                        cursor.getDouble(Points.INDEX_Y) * mDisplayUnitsFactor,
                        cursor.getDouble(Points.INDEX_X) * mDisplayUnitsFactor));
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

        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onCreateLoader");
        CursorLoader loader =  new CursorLoader(
                getActivity(),          // Parent activity context
                Uri.parse(Points.CONTENT_URI),
                Points.PROJECTION,      // Projection to return
                null,                   // No selection clause
                null,                    // No selection arguments
                null                    // Default sort order
        );

        // The loader will wait 0.5 seconds between requerys.
        loader.setUpdateThrottle(500);
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoadFinished");
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
