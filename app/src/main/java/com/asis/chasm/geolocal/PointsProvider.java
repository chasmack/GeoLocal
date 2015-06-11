package com.asis.chasm.geolocal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class PointsProvider extends ContentProvider {

    // Use for logging and debugging
    private static final String TAG = "PointsProvider";

    // A UriMatcher instance
    private static final UriMatcher sUriMatcher;

    // A new Database Helper
    private PointsDbHelper mDbHelper;

    static {
        // Create and initialize the UriMatcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    }

    static class PointsDbHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "points.db";
        public static final int DATABASE_VERSION = 1;

        private static final String COMMA_SEP = ", ";

        private static final String SQL_CREATE_POINTS =
                "CREATE TABLE " + PointsContract.Points.TABLE + " ("
                        + PointsContract.Points._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PointsContract.Points.COLUMN_PT_NAME + " TEXT" + COMMA_SEP
                        + PointsContract.Points.COLUMN_PT_DESC + " TEXT" + COMMA_SEP
                        + PointsContract.Points.COLUMN_PT_TYPE + " INTEGER" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LOCAL_X + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LOCAL_Y + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LAT + " REAL" + COMMA_SEP
                        + PointsContract.Points.COLUMN_LON + " REAL"
                        + ")";

        private static final String SQL_CREATE_COORD_SYSTEMS =
                "CREATE TABLE " + PointsContract.Points.TABLE + " ("
                        + PointsContract.CoordSystems._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PointsContract.CoordSystems.COLUMN_CODE + " TEXT" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_DESC + " TEXT" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_TYPE + " INTEGER" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_PROJ + " INTEGER" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_P0 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_M0 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_X0 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_Y0 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_P1 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_P2 + " REAL" + COMMA_SEP
                        + PointsContract.CoordSystems.COLUMN_SF + " INTEGER"
                        + ")";

        private static final String SQL_CREATE_TRANSFORM =
                "CREATE TABLE " + PointsContract.Points.TABLE + " ("
                        + PointsContract.Transform.COLUMN_REF_X + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_REF_Y + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_REF_LAT + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_REF_LON + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_ROTATE + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_SCALE + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_UNITS + " INTEGER" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_COORD_SYSTEM_CODE + " TEXT" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_COORD_SYSTEM_TYPE + " INTEGER" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_COORD_SYSTEM_PROJ + " INTEGER" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_P0 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_M0 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_X0 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_Y0 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_P1 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_P2 + " REAL" + COMMA_SEP
                        + PointsContract.Transform.COLUMN_SF + " INTEGER"
                        + ")";

        private static final String SQL_DROP_POINTS =
                "DROP TABLE IF EXISTS " + PointsContract.Points.TABLE;

        private static final String SQL_DROP_COORD_SYSTEMS =
                "DROP TABLE IF EXISTS " + PointsContract.CoordSystems.TABLE;

        private static final String SQL_DROP_TRANSFORM =
                "DROP TABLE IF EXISTS " + PointsContract.Transform.TABLE;

        public PointsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db){
            db.execSQL(SQL_CREATE_POINTS);
            db.execSQL(SQL_CREATE_COORD_SYSTEMS);
            db.execSQL(SQL_CREATE_TRANSFORM);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_POINTS);
            db.execSQL(SQL_DROP_COORD_SYSTEMS);
            db.execSQL(SQL_DROP_TRANSFORM);
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
        return false;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
