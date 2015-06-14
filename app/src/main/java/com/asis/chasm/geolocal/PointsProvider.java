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

         /*
         * Creates and initializes a projection map that returns all columns
         */

        sPointsProjectionMap = new HashMap<String, String>();
        sPointsProjectionMap.put(PointsContract.Points._ID, PointsContract.Points._ID);
        sPointsProjectionMap.put(PointsContract.Points.COLUMN_NAME, PointsContract.Points.COLUMN_DESC);

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
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PointsContract.Points.TABLE);

        /**
         * Choose the projection and adjust the "where" clause based on URI pattern-matching.
         */
        switch (sUriMatcher.match(uri)) {
            case POINTS:
                qb.setProjectionMap(sPointsProjectionMap);
                break;
            case POINTS_ID:
                qb.setProjectionMap(sPointsProjectionMap);
                qb.appendWhere(
                        PointsContract.Points._ID +
                                "=" + uri.getLastPathSegment());
                break;
            case PROJECTIONS:
            case PROJECTIONS_ID:
            case TRANSFORMS:
            case TRANSFORMS_ID:
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = PointsContract.Points.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Opens the database object in "read" mode, since no writes need to be done.
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

       /*
        * Performs the query. If no problems occur trying to read the database, then a Cursor
        * object is returned; otherwise, the cursor variable contains null. If no records were
        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
        */
        Cursor c = qb.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                orderBy        // The sort order
        );

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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete Uri: " + uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        String id;
        switch (match) {
            case POINTS_ID:
                id = PointsContract.Points._ID + " = " + uri.getLastPathSegment();
                selection = selection != null ? id + " AND " + selection : id;
                // FALL THROUGH
            case POINTS:
                return db.delete(PointsContract.Points.TABLE, selection, selectionArgs);

            case PROJECTIONS_ID:
                id = PointsContract.Projections._ID + " = " + uri.getLastPathSegment();
                selection = selection != null ? id + " AND " + selection : id;
                // FALL THROUGH
            case PROJECTIONS:
                return db.delete(PointsContract.Projections.TABLE, selection, selectionArgs);

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
