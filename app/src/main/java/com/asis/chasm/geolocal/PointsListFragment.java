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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.asis.chasm.geolocal.PointsContract.Points;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PointsListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ListFragment";

    // Points type to display in points list.
    private static final int COORDINATE_TYPE_LOCAL = 1;
    private static final int COORDINATE_TYPE_GEOGRAPHIC = 2;

    // This is the Adapter being used to display the list's data.
    private PointsCursorAdapter mAdapter;

    // A hook back into main activity.
    private OnListFragmentInteractionListener mListener;

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
        Log.d(TAG, "onActivityCreated bundle="
                + (savedInstanceState == null ? "null" : savedInstanceState.toString()));

        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No points.");

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new PointsCursorAdapter(getActivity(), null, 0);

        // Set onClickListeners for the Local/Geographic radio buttons
        RadioButton local = (RadioButton) getActivity().findViewById(R.id.radio_local);
        local.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAdapter.setCoordinateType(COORDINATE_TYPE_LOCAL);
            }
        });

        RadioButton geo = (RadioButton) getActivity().findViewById(R.id.radio_geographic);
        geo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAdapter.setCoordinateType(COORDINATE_TYPE_GEOGRAPHIC);
            }
        });

        // Connect the cursor adapter to the points list.
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);

        // Initialize the coordinate type setting default if necessary.
        RadioGroup group = (RadioGroup) getActivity().findViewById(R.id.radio_group);
        int checked = group.getCheckedRadioButtonId();
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radio_local:
                mAdapter.setCoordinateType(COORDINATE_TYPE_LOCAL);
                break;
            case R.id.radio_geographic:
                mAdapter.setCoordinateType(COORDINATE_TYPE_GEOGRAPHIC);
                break;
            default:
                group.check(R.id.radio_local);
                mAdapter.setCoordinateType(COORDINATE_TYPE_LOCAL);
                break;
        }

        // Add a units label to the Local Coordinates radio button
        TextView tv = (TextView) getView().findViewById(R.id.radio_local);
        tv.setText("Local (" + TransformSettings.getSettings().getUnitsName() + ")");

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
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
        return super.onOptionsItemSelected(item);
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

    private static class PointsCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;
        private int mCoordinateType;

        public PointsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        private void setCoordinateType(int type) {
            mCoordinateType = type;
            notifyDataSetChanged();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv;
            tv = (TextView) view.findViewById(R.id.name);
            tv.setText(cursor.getString(Points.INDEX_NAME));
            tv = (TextView) view.findViewById(R.id.desc);
            tv.setText(cursor.getString(Points.INDEX_DESC));


            TransformSettings settings = TransformSettings.getSettings();
            switch (mCoordinateType) {

                case COORDINATE_TYPE_GEOGRAPHIC:
                    GeoPoint geo = new LocalPoint(
                            cursor.getDouble(Points.INDEX_X),
                            cursor.getDouble(Points.INDEX_Y)).toGrid().toGeo();

                    tv = (TextView) view.findViewById(R.id.coord_values);
                    tv.setText(String.format(settings.getGeographicCoordFormat(),
                            geo.getLat(), geo.getLon()));
                    tv = (TextView) view.findViewById(R.id.coord_prefix);
                    tv.setText("lat/lon:");
                    break;

                case COORDINATE_TYPE_LOCAL:
                    tv = (TextView) view.findViewById(R.id.coord_values);
                    tv.setText(String.format(settings.getLocalCoordFormat(),
                            cursor.getDouble(Points.INDEX_Y) * settings.getUnitsFactor(),
                            cursor.getDouble(Points.INDEX_X) * settings.getUnitsFactor()));
                    tv =(TextView) view.findViewById(R.id.coord_prefix);
                    tv.setText("N/E:");
                    break;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onCreateLoader");
        CursorLoader loader =  new CursorLoader(
                getActivity(),          // Parent activity context
                Uri.parse(Points.CONTENT_URI),
                Points.PROJECTION,      // Projection to return
                null,                   // No selection clause
                null,                   // No selection arguments
                null                    // Default sort order
        );

        // The loader will wait 0.5 seconds between requerys.
        loader.setUpdateThrottle(500);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoadFinished");
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "LoaderManager.LoaderCallbacks<?>.onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
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
    public interface OnListFragmentInteractionListener {
        public void onListFragmentInteraction(long position);
    }
}
