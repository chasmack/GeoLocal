package com.asis.chasm.geolocal;

import android.provider.BaseColumns;

/**
 *  Sqlite contract class for the points database.
 */

public final class PointsContract {

    private PointsContract() { }

    public static final String DATABASE_NAME = "points.db";
    public static final int DATABASE_VERSION = 1;

    public static final String AUTHORITY = "com.asis.chasm.provider.Points";
    public static final String BASE_URI = "content://" + AUTHORITY + "/";

    /*
    * Provider specific call methods.
    */

    // GET_COUNT - get the count of items table passed as an arguement
    public static final String CALL_GET_COUNT_METHOD = "get_count";
    public static final String CALL_GET_COUNT_EXTRAS_COLUMN = "column";
    public static final String CALL_GET_COUNT_EXTRAS_ARGS = "args";
    public static final String CALL_GET_COUNT_RESULT_KEY = "count";

    private static final String COMMA_SEP = ", ";

    /*
    * Points table definitions.
    */

    public static class Points implements BaseColumns {

        public static final String CONTENT_PATH = "points";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.point";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.point";

        public static final String TABLE = "points";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";

        public static final int INDEX_ID = 0;
        public static final int INDEX_NAME = 1;
        public static final int INDEX_DESC = 2;
        public static final int INDEX_X = 3;
        public static final int INDEX_Y = 4;

        public static final String SQL_CREATE_TABLE=
                "CREATE TABLE " + Points.TABLE + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_NAME + " TEXT" + COMMA_SEP
                        + COLUMN_DESC + " TEXT" + COMMA_SEP
                        + COLUMN_X    + " REAL" + COMMA_SEP
                        + COLUMN_Y    + " REAL" + ")";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Points.TABLE;

        public static final int TYPE_LOCAL = 0;
        public static final int TYPE_GEOGRAPHIC = 1;

        public static final String[] PROJECTION = {
                _ID, COLUMN_NAME, COLUMN_DESC, COLUMN_X, COLUMN_Y
        };

        // Default sort order sorts numerically.
        public static final String DEFAULT_ORDER_BY = "CAST(" + COLUMN_NAME + " AS INTEGER), "
                + "SUBSTR(" + COLUMN_NAME + ",1,1), "
                + "CAST(SUBSTR(" + COLUMN_NAME + ",2) AS INTEGER), "
                + COLUMN_NAME;
    }

    /*
    * GeoPoints table definitions.
    */

    public static abstract class GeoPoints implements BaseColumns {

        public static final String CONTENT_PATH = "geopoints";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.geopoint";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.geopoint";

        public static final String TABLE = "geopoints";

        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CMT = "cmt";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_SAMPLES = "samples";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + GeoPoints.TABLE + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_LAT     + " REAL" + COMMA_SEP
                        + COLUMN_LON     + " REAL" + COMMA_SEP
                        + COLUMN_TIME    + " TEXT" + COMMA_SEP
                        + COLUMN_NAME    + " TEXT" + COMMA_SEP
                        + COLUMN_CMT     + " TEXT" + COMMA_SEP
                        + COLUMN_DESC    + " TEXT" + COMMA_SEP
                        + COLUMN_SYMBOL  + " TEXT" + COMMA_SEP
                        + COLUMN_SAMPLES + " INTEGER" + ")";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + GeoPoints.TABLE;

        public static final int INDEX_ID = 0;
        public static final int INDEX_LAT = 1;
        public static final int INDEX_LON = 2;
        public static final int INDEX_TIME = 3;
        public static final int INDEX_NAME = 4;
        public static final int INDEX_CMT = 5;
        public static final int INDEX_DESC = 6;
        public static final int INDEX_SYMBOL = 7;
        public static final int INDEX_SAMPLES = 8;

        // Default sort order sorts numerically.
        public static final String DEFAULT_ORDER_BY = "CAST(" + COLUMN_NAME + " AS INTEGER), "
                + "SUBSTR(" + COLUMN_NAME + ",1,1), "
                + "CAST(SUBSTR(" + COLUMN_NAME + ",2) AS INTEGER), "
                + COLUMN_NAME;
    }

    /*
    * Projections table definitions.
    */

    public static abstract class Projections implements BaseColumns {

        public static final String CONTENT_PATH = "projections";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.projections";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.projections";

        public static final String TABLE = "projections";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_SYSTEM = "system";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_P0 = "p0";
        public static final String COLUMN_M0 = "m0";
        public static final String COLUMN_X0 = "x0";
        public static final String COLUMN_Y0 = "y0";
        public static final String COLUMN_P1 = "p1";
        public static final String COLUMN_P2 = "p2";
        public static final String COLUMN_K0 = "k0";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + Projections.TABLE + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_CODE   + " TEXT" + COMMA_SEP
                        + COLUMN_DESC   + " TEXT" + COMMA_SEP
                        + COLUMN_SYSTEM + " TEXT" + COMMA_SEP
                        + COLUMN_TYPE   + " TEXT" + COMMA_SEP
                        + COLUMN_P0     + " REAL" + COMMA_SEP
                        + COLUMN_M0     + " REAL" + COMMA_SEP
                        + COLUMN_X0     + " REAL" + COMMA_SEP
                        + COLUMN_Y0     + " REAL" + COMMA_SEP
                        + COLUMN_P1     + " REAL" + COMMA_SEP
                        + COLUMN_P2     + " REAL" + COMMA_SEP
                        + COLUMN_K0     + " REAL" + ")";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Projections.TABLE;

        public static final int INDEX_ID = 0;
        public static final int INDEX_CODE = 1;
        public static final int INDEX_DESC = 2;
        public static final int INDEX_SYSTEM = 3;
        public static final int INDEX_TYPE = 4;
        public static final int INDEX_P0 = 5;
        public static final int INDEX_M0 = 6;
        public static final int INDEX_X0 = 7;
        public static final int INDEX_Y0 = 8;
        public static final int INDEX_P1 = 9;
        public static final int INDEX_P2 = 10;
        public static final int INDEX_K0 = 11;

        public static final String SYSTEM_SPCS = "spcs";
        public static final String SYSTEM_UTM = "utm";
        public static final String SYSTEM_USER = "user";

        // This list if system ids is used to build the projection systems
        // spinner list in the projections preference dialog. To display an
        // option for USER systems add SYSTEM_USER to the list.
        public static final String[] SYSTEM_IDS = new String[] {
                SYSTEM_SPCS, SYSTEM_UTM, SYSTEM_USER
        };

        public static final String TYPE_LC = "lc";    // Lambert Conic
        public static final String TYPE_TM = "tm";    // Transverse Mercator
        public static final String TYPE_OM = "om";    // Oblique Mercator

        // Abbreviated projection excluding numerical projection constants.
        public static final String[] PROJECTION_SHORT = {
                _ID, COLUMN_CODE, COLUMN_DESC, COLUMN_SYSTEM, COLUMN_TYPE
        };

        // Full projection returning all of the fields.
        public static final String[] PROJECTION_FULL = null;

        // Default sorts by order projections were inserted into the database.
        public static final String DEFAULT_ORDER_BY = _ID;
    }
}
