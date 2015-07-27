package com.asis.chasm.geolocal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

import com.asis.chasm.geolocal.PointsContract.Points;
import com.asis.chasm.geolocal.PointsContract.Projections;

public class PointsProvider extends ContentProvider {

    // Use for logging and debugging
    private static final String TAG = "PointsProvider";

    // A UriMatcher instance
    private static final UriMatcher sUriMatcher;

    // A projection map used to select columns from the database
    private static HashMap<String, String> sPointsProjectionMap;

    // A new Database Helper
    private PointsDbHelper mDbHelper;

    // Constants for UriMatcher to return for matched Uris
    private static final int POINTS = 1;
    private static final int POINTS_ID = 2;
    private static final int PROJECTIONS = 3;
    private static final int PROJECTIONS_ID = 4;

    static {

        // Create and initialize the UriMatcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add Uris for Points, CoordSystems and Transforms
        sUriMatcher.addURI(PointsContract.AUTHORITY, Points.CONTENT_PATH, POINTS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, Points.CONTENT_PATH + "/#", POINTS_ID);
        sUriMatcher.addURI(PointsContract.AUTHORITY, Projections.CONTENT_PATH, PROJECTIONS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, Projections.CONTENT_PATH + "/#", PROJECTIONS_ID);
    }

    class PointsDbHelper extends SQLiteOpenHelper {

        public PointsDbHelper(Context context) {
            super(context, PointsContract.DATABASE_NAME, null, PointsContract.DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db){
            db.execSQL(Points.SQL_CREATE_TABLE);
            db.execSQL(Projections.SQL_CREATE_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(Points.SQL_DROP_TABLE);
            db.execSQL(Projections.SQL_DROP_TABLE);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public PointsProvider() {
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PointsDbHelper(getContext());
        return true;
    }

    /*
    * call - provider specific utility methods.
    */

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        switch (method) {
            case PointsContract.CALL_GET_COUNT_METHOD:
                return getCount(arg, extras);
        }
        return null;
    }

    private Bundle getCount(String arg, Bundle extras) {

        // Argument string is the table name and is required.
        if (arg == null) return null;

        String query = "select count(*) as count from " + arg;
        String[] selectionArgs = null;

        // Check for extras
        if (extras != null) {
            String column = extras.getString(PointsContract.CALL_GET_COUNT_EXTRAS_COLUMN);
            selectionArgs = extras.getStringArray(PointsContract.CALL_GET_COUNT_EXTRAS_ARGS);
            if (column != null && selectionArgs != null) {
                query += " where " + column + "=?";
            }
        }

        Bundle result = new Bundle();
        int count = 0;

        Cursor c = mDbHelper.getReadableDatabase().rawQuery(query, selectionArgs);
        if (c.moveToFirst())
            count = c.getInt(c.getColumnIndex("count"));
        c.close();

        Log.d(TAG, "GET_COUNT table=" + arg + " count=" + count);
        result.putInt(PointsContract.CALL_GET_COUNT_RESULT_KEY, count);

        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String select,
                        String[] selectArgs, String sort) {

        String table;
        String fullSelect;
        String orderBy = null;
        switch (sUriMatcher.match(uri))  {

            case POINTS:
                table = Points.TABLE;
                fullSelect = select;
                if (sort == null || sort.isEmpty()) {
                    orderBy = Points.DEFAULT_ORDER_BY;
                } else {
                    orderBy = sort;
                }
                break;
            case POINTS_ID:
                table = Points.TABLE;
                fullSelect = Points._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                break;

            case PROJECTIONS:
                table = Projections.TABLE;
                fullSelect = select;
                if (sort == null || sort.isEmpty()) {
                    orderBy = Projections.DEFAULT_ORDER_BY;
                } else {
                    orderBy = sort;
                }
                break;
            case PROJECTIONS_ID:
                table = Projections.TABLE;
                fullSelect = Projections._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

       /*
        * Performs the query. If no problems occur trying to read the database, then a Cursor
        * object is returned; otherwise, the cursor variable contains null. If no records were
        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
        */
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(
                table,   // The database to query
                projection,     // The columns to return from the query
                fullSelect,        // The columns for the where clause
                selectArgs,     // The values for the where clause
                null,           // don't group the rows
                null,           // don't filter by row groups
                orderBy         // The sort order
        );
        Log.d(TAG, "query table \"" + table + "\" rows x columns = " + c.getCount() + " x " + c.getColumnCount());

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        // Choose a MIME type of base on a Uri.
        Log.d(TAG, "getType Uri: " + uri);
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                return Points.CONTENT_TYPE;
            case POINTS_ID:
                return Points.CONTENT_TYPE_ITEM;
            case PROJECTIONS:
                return Projections.CONTENT_TYPE;
            case PROJECTIONS_ID:
                return Projections.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String table;
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                table = Points.TABLE;
                break;
            case PROJECTIONS:
                table = Projections.TABLE;
                break;
            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }

        // Perform the insert.
        long id = mDbHelper.getWritableDatabase().insert(table, null, values);

        // Let the content resolver know the data has changed.
        Uri result = uri.buildUpon().appendPath(Long.toString(id)).build();
        getContext().getContentResolver().notifyChange(result, null);

        Log.d(TAG, "insert into \"" + table + "\": " + result.toString());
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        String table;
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                table = Points.TABLE;
                break;
            case PROJECTIONS:
                table = Projections.TABLE;
                break;
            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }

        // Perform the inserts
        int cnt = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for (ContentValues v : values) {
            if (db.insert(table, null, v) > 0)
                cnt++;
        }

        // Let the content resolver know the data has changed.
        getContext().getContentResolver().notifyChange(uri, null);

        Log.d(TAG, "bulkInsert into \"" + table + "\" count: " + cnt);
        return cnt;
    }

    @Override
    public int delete(Uri uri, String select, String[] selectArgs) {
        Log.d(TAG, "delete Uri: " + uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String fullSelect;
        int rows;
        switch (sUriMatcher.match(uri)) {

            case POINTS:
                rows = db.delete(Points.TABLE, select, selectArgs);
                break;

            case POINTS_ID:
                fullSelect = Points._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                rows = db.delete(Points.TABLE, fullSelect, selectArgs);
                break;

            case PROJECTIONS:
                rows = db.delete(Projections.TABLE, select, selectArgs);
                break;

            case PROJECTIONS_ID:
                fullSelect = Projections._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                rows = db.delete(Projections.TABLE, fullSelect, selectArgs);
                break;

            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }

        // Let the content resolver know the data has changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        // TODO: Implement content resolver update requests.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
