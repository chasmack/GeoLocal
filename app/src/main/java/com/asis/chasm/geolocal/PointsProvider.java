package com.asis.chasm.geolocal;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

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
    private static final int TRANSFORMS = 5;
    private static final int TRANSFORMS_ID = 6;

    static {

        // Create and initialize the UriMatcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add Uris for Points, CoordSystems and Transforms
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Points.CONTENT_PATH, POINTS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Points.CONTENT_PATH + "/#", POINTS_ID);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Projections.CONTENT_PATH, PROJECTIONS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Projections.CONTENT_PATH + "/#", PROJECTIONS_ID);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Transforms.CONTENT_PATH, TRANSFORMS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Transforms.CONTENT_PATH + "/#", TRANSFORMS_ID);

    }

    class PointsDbHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "points.db";
        public static final int DATABASE_VERSION = 2;

        private static final String COMMA_SEP = ", ";

        private static final String SQL_CREATE_POINTS =
                "CREATE TABLE " + PointsContract.Points.TABLE + " ("
                        + PointsContract.Points._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PointsContract.Points.COLUMN_NAME + " TEXT" + COMMA_SEP
                        + PointsContract.Points.COLUMN_DESC + " TEXT" + COMMA_SEP
                        + PointsContract.Points.COLUMN_TYPE + " INTEGER" + COMMA_SEP
                        + PointsContract.Points.COLUMN_X + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_Y + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LAT + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LON + " REAL"
                        + ")";

        private static final String SQL_CREATE_PROJECTIONS =
                "CREATE TABLE " + PointsContract.Projections.TABLE + " ("
                        + PointsContract.Projections._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PointsContract.Projections.COLUMN_CODE + " TEXT" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_DESC + " TEXT" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_COORD_SYSTEM + " INTEGER" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_PROJECTION + " INTEGER" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_P0 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_M0 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_X0 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_Y0 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_P1 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_P2 + " REAL" + COMMA_SEP
                        + PointsContract.Projections.COLUMN_SF + " INTEGER"
                        + ")";

        private static final String SQL_CREATE_TRANSFORMS =
                "CREATE TABLE " + PointsContract.Transforms.TABLE + " ("
                        + PointsContract.Transforms._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PointsContract.Transforms.COLUMN_UNITS + " INTEGER" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_REF_X + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_REF_Y + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_REF_LAT + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_REF_LON + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_ROTATE + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_SCALE + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_CODE + " TEXT" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_DESC + " TEXT" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_COORD_SYSTEM + " INTEGER" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_PROJECTION + " INTEGER" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_P0 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_M0 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_X0 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_Y0 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_P1 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_P2 + " REAL" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_SF + " INTEGER"
                        + ")";

        private static final String SQL_DROP_POINTS =
                "DROP TABLE IF EXISTS " + PointsContract.Points.TABLE;

        private static final String SQL_DROP_PROJECTIONS =
                "DROP TABLE IF EXISTS " + PointsContract.Projections.TABLE;

        private static final String SQL_DROP_TRANSFORMS =
                "DROP TABLE IF EXISTS " + PointsContract.Transforms.TABLE;

        public PointsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db){
            // Create the tables
            db.execSQL(SQL_CREATE_POINTS);
            db.execSQL(SQL_CREATE_PROJECTIONS);
            db.execSQL(SQL_CREATE_TRANSFORMS);


        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_POINTS);
            db.execSQL(SQL_DROP_PROJECTIONS);
            db.execSQL(SQL_DROP_TRANSFORMS);
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

    @Override
    public Cursor query(Uri uri, String[] projection, String select,
                        String[] selectArgs, String sort) {

        String fullSelect;
        switch (sUriMatcher.match(uri))  {

            case POINTS:
                fullSelect = select;
                break;

            case POINTS_ID:
                fullSelect = PointsContract.Points._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                break;

            case PROJECTIONS:
            case PROJECTIONS_ID:
            case TRANSFORMS:
            case TRANSFORMS_ID:
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // If no sort order is specified, uses the default
        String orderBy;
        if (sort == null || sort.isEmpty()) {
            orderBy = PointsContract.Points.DEFAULT_ORDER_BY;
        } else {
            orderBy = sort;
        }

       /*
        * Performs the query. If no problems occur trying to read the database, then a Cursor
        * object is returned; otherwise, the cursor variable contains null. If no records were
        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
        */
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(
                PointsContract.Points.TABLE,    // The database to query
                projection,      // The columns to return from the query
                fullSelect,      // The columns for the where clause
                selectArgs,      // The values for the where clause
                null,            // don't group the rows
                null,            // don't filter by row groups
                orderBy          // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "query rows: " + c.getCount());
        Log.d(TAG, "query columns: " + c.getColumnCount());
        return c;
    }

    @Override
    public String getType(Uri uri) {
        // Choose a MIME type of base on a Uri.
        Log.d(TAG, "getType Uri: " + uri);
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                return PointsContract.Points.CONTENT_TYPE;
            case POINTS_ID:
                return PointsContract.Points.CONTENT_TYPE_ITEM;
            case PROJECTIONS:
                return PointsContract.Projections.CONTENT_TYPE;
            case PROJECTIONS_ID:
                return PointsContract.Projections.CONTENT_TYPE_ITEM;
            case TRANSFORMS:
                return PointsContract.Transforms.CONTENT_TYPE;
            case TRANSFORMS_ID:
                return PointsContract.Transforms.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id;
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                id = db.insert(PointsContract.Points.TABLE, null, values);
                return Uri.parse(PointsContract.Points.CONTENT_URI)
                        .buildUpon().appendPath(Long.toString(id)).build();

            case PROJECTIONS:
                id = db.insert(PointsContract.Projections.TABLE, null, values);
                return Uri.parse(PointsContract.Projections.CONTENT_URI)
                        .buildUpon().appendPath(Long.toString(id)).build();

            case TRANSFORMS:
                return null;

            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String select, String[] selectArgs) {
        Log.d(TAG, "delete Uri: " + uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String fullSelect;
        switch (sUriMatcher.match(uri)) {

            case POINTS:
                return db.delete(PointsContract.Points.TABLE, select, selectArgs);

            case POINTS_ID:
                fullSelect = PointsContract.Points._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                return db.delete(PointsContract.Points.TABLE, fullSelect, selectArgs);

            case PROJECTIONS:
                return db.delete(PointsContract.Projections.TABLE, select, selectArgs);

            case PROJECTIONS_ID:
                fullSelect = PointsContract.Projections._ID + "=" + uri.getLastPathSegment();
                if (select != null && !select.isEmpty()) {
                    fullSelect = select + " AND " + fullSelect;
                }
                return db.delete(PointsContract.Projections.TABLE, fullSelect, selectArgs);

            case TRANSFORMS_ID:
            case TRANSFORMS:
                return 0;

            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
