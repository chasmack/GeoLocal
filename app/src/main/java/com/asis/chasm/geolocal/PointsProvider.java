package com.asis.chasm.geolocal;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PointsProvider extends ContentProvider {

    // Use for logging and debugging
    private static final String TAG = "PointsProvider";

    // A UriMatcher instance
    private static final UriMatcher sUriMatcher;

    // A new Database Helper
    private PointsDbHelper mDbHelper;

    // Constants for UriMatcher to return for matched Uris
    private static final int POINTS = 1;
    private static final int POINTS_ID = 2;
    private static final int COORD_SYSTEMS = 3;
    private static final int COORD_SYSTEMS_ID = 4;
    private static final int TRANSFORMS = 5;
    private static final int TRANSFORMS_ID = 6;

    static {

        // Create and initialize the UriMatcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add Uris for Points, CoordSystems and Transforms
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Points.CONTENT_PATH, POINTS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Points.CONTENT_PATH + "/#", POINTS_ID);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.CoordSystems.CONTENT_PATH, COORD_SYSTEMS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.CoordSystems.CONTENT_PATH + "/#", COORD_SYSTEMS_ID);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Transforms.CONTENT_PATH, TRANSFORMS);
        sUriMatcher.addURI(PointsContract.AUTHORITY, PointsContract.Transforms.CONTENT_PATH + "/#", TRANSFORMS_ID);

    }

    class PointsDbHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "points.db";
        public static final int DATABASE_VERSION = 1;

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

        private static final String SQL_CREATE_COORD_SYSTEMS =
                "CREATE TABLE " + PointsContract.CoordSystems.TABLE + " ("
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
                        + PointsContract.Transforms.COLUMN_COORD_SYSTEM_CODE + " TEXT" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_COORD_SYSTEM_TYPE + " INTEGER" + COMMA_SEP
                        + PointsContract.Transforms.COLUMN_COORD_SYSTEM_PROJ + " INTEGER" + COMMA_SEP
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

        private static final String SQL_DROP_COORD_SYSTEMS =
                "DROP TABLE IF EXISTS " + PointsContract.CoordSystems.TABLE;

        private static final String SQL_DROP_TRANSFORMS =
                "DROP TABLE IF EXISTS " + PointsContract.Transforms.TABLE;

        public PointsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db){
            // Create the tables
            db.execSQL(SQL_CREATE_POINTS);
            db.execSQL(SQL_CREATE_COORD_SYSTEMS);
            db.execSQL(SQL_CREATE_TRANSFORMS);


        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_POINTS);
            db.execSQL(SQL_DROP_COORD_SYSTEMS);
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

        // Populate Coordinate Systems table
        populateCoordSystems(mDbHelper.getWritableDatabase(),
                "spcs-zones.txt", PointsContract.CoordSystems.TYPE_SPCS);

        return true;
    }

    private void populateCoordSystems(SQLiteDatabase db,
            String assetFile, int coordSystemType) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open(assetFile), "UTF-8"));

            int cnt;
            String line;
            String[] parts;
            ContentValues values = new ContentValues();

            // Delete any entries already in the Coordinate Systems table
            cnt = db.delete(PointsContract.CoordSystems.TABLE, null, null);
            Log.d(TAG, "Rows deleted: " + cnt);

            cnt = 0;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#"))
                    continue;       // comment lines start with #
                cnt++;

                parts = line.split(",", 10);
                if (parts.length != 10) {
                    Log.d(TAG, "File format error: " + line);
                    break;
                }

                // 0-CODE, 1-DESC, 2-PROJ, 3-P0, 4-M0, 5-X0, 6-Y0, 7-P1, 8-P2, 9-SF
                values.put(PointsContract.CoordSystems.COLUMN_CODE, parts[0]);
                values.put(PointsContract.CoordSystems.COLUMN_DESC, parts[1]);
                values.put(PointsContract.CoordSystems.COLUMN_TYPE, coordSystemType);
                switch (parts[2]) {
                    case "L":
                        values.put(PointsContract.CoordSystems.COLUMN_PROJ,
                                PointsContract.CoordSystems.PROJ_LC);
                        break;
                    case "T":
                        values.put(PointsContract.CoordSystems.COLUMN_PROJ,
                                PointsContract.CoordSystems.PROJ_TM);
                        break;
                    case "O":
                        values.put(PointsContract.CoordSystems.COLUMN_PROJ,
                                PointsContract.CoordSystems.PROJ_OM);
                        break;
                    default:
                        values.put(PointsContract.CoordSystems.COLUMN_PROJ,
                                PointsContract.CoordSystems.PROJ_OTHER);
                        break;
                }
                values.put(PointsContract.CoordSystems.COLUMN_PROJ, parts[2]);
                values.put(PointsContract.CoordSystems.COLUMN_P0, parseDegMin(parts[3]));
                values.put(PointsContract.CoordSystems.COLUMN_M0, parseDegMin(parts[4]));
                values.put(PointsContract.CoordSystems.COLUMN_X0,
                        parts[5].isEmpty() ? 0.0 : Double.parseDouble(parts[5]));
                values.put(PointsContract.CoordSystems.COLUMN_Y0,
                        parts[5].isEmpty() ? 0.0 :  Double.parseDouble(parts[6]));
                values.put(PointsContract.CoordSystems.COLUMN_P1, parseDegMin(parts[7]));
                values.put(PointsContract.CoordSystems.COLUMN_P2, parseDegMin(parts[8]));
                values.put(PointsContract.CoordSystems.COLUMN_SF,
                        parts[9].isEmpty() ? 0 : Long.parseLong(parts[9]));

                db.insert(PointsContract.CoordSystems.TABLE, null, values);
            }
            Log.d(TAG, "Transforms loaded: " + cnt);

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
    }

    private double parseDegMin(String dmString) {
        double val;
        String[] parts;
        boolean neg = false;

        if (dmString == null) {
            throw new IllegalArgumentException("Null DEG-MIN string.");
        }
        if (dmString.isEmpty()) {
            return 0.0;                 // empty string is valid
        }
        if (dmString.startsWith("-")) {
            neg = true;
            parts = dmString.substring(1).split("-");
        } else {
            parts = dmString.split("-");
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid DEG-MIN string: " + dmString);
        }
        val = Integer.parseInt(parts[0]) + Integer.parseInt(parts[1])/60.0;

        return neg ? -1.0 * val : val;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
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
            case COORD_SYSTEMS:
                return PointsContract.CoordSystems.CONTENT_TYPE;
            case COORD_SYSTEMS_ID:
                return PointsContract.CoordSystems.CONTENT_TYPE_ITEM;
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
