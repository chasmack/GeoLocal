package com.asis.chasm.geolocal;

import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.dummy.DummyContent;

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

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new PointsCursorAdapter(getActivity(), null, 0);
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
