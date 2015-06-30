package com.asis.chasm.geolocal;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PointsManagerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PointsManagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PointsManagerFragment extends Fragment {

    // Use for logging and debugging
    private static final String TAG = "PointsManagerFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PointsManagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PointsManagerFragment newInstance(String param1, String param2) {
        PointsManagerFragment fragment = new PointsManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PointsManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        public void onPointsManagerFragmentInteraction(int value);
    }

}
