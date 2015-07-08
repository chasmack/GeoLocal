package com.asis.chasm.geolocal;

import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 *  Sqlite contract class for the points database.
 */

public final class PointsContract {

    private PointsContract() {}

    public static final String AUTHORITY = "com.asis.chasm.provider.Points";
    public static final String BASE_URI = "content://" + AUTHORITY + "/";

    public static abstract class Points implements BaseColumns {

        public static final int TYPE_LOCAL = 1;
        public static final int TYPE_GEOGRAPHIC = 2;

        public static final String CONTENT_PATH = "points";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.point";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.point";

        public static final String TABLE = "points";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";

        public static final int INDEX_NAME = 1;
        public static final int INDEX_DESC = 2;
        public static final int INDEX_TYPE = 3;
        public static final int INDEX_X = 4;
        public static final int INDEX_Y = 5;
        public static final int INDEX_LAT = 6;
        public static final int INDEX_LON = 7;

        public static final String[] PROJECTION = { _ID,
                COLUMN_NAME, COLUMN_DESC, COLUMN_TYPE,
                COLUMN_X, COLUMN_Y, COLUMN_LAT, COLUMN_LON
        };

        // Default sort order sorts numerically.
        public static final String DEFAULT_ORDER_BY = "CAST(" + COLUMN_NAME + " AS INTEGER), "
                + "SUBSTR(" + COLUMN_NAME + ",1,1), "
                + "CAST(SUBSTR(" + COLUMN_NAME + ",2) AS INTEGER), "
                + COLUMN_NAME;
    }

    public static abstract class Projections implements BaseColumns {

        public static final String CONTENT_PATH = "projections";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.projections";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.projections";

        public static final String TABLE = "projections";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_COORD_SYSTEM = "system";
        public static final String COLUMN_PROJECTION = "proj";
        public static final String COLUMN_P0 = "p0";
        public static final String COLUMN_M0 = "m0";
        public static final String COLUMN_X0 = "x0";
        public static final String COLUMN_Y0 = "y0";
        public static final String COLUMN_P1 = "p1";
        public static final String COLUMN_P2 = "p2";
        public static final String COLUMN_K0 = "k0";

        public static final int INDEX_CODE = 1;
        public static final int INDEX_DESC = 2;
        public static final int INDEX_COORD_SYSTEM = 3;
        public static final int INDEX_PROJECTION = 4;
        public static final int INDEX_P0 = 5;
        public static final int INDEX_M0 = 6;
        public static final int INDEX_X0 = 7;
        public static final int INDEX_Y0 = 8;
        public static final int INDEX_P1 = 9;
        public static final int INDEX_P2 = 10;
        public static final int INDEX_K0 = 11;

        public static final int COORD_SYSTEM_UTM = 1;
        public static final int COORD_SYSTEM_SPCS = 2;
        public static final int COORD_SYSTEM_USER = 3;

        public static final int PROJECTION_TM = 1;    // Transverse Mercator
        public static final int PROJECTION_LC = 2;    // Lambert Conic
        public static final int PROJECTION_OM = 3;    // Oblique Mercator

        // Default sort order sorts numerically.
        public static final String DEFAULT_ORDER_BY = null;
    }

    public static abstract class Transforms implements BaseColumns {

        public static final String CONTENT_PATH = "transforms";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.transform";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.transform";

        public static final String TABLE = "transforms";
        public static final String COLUMN_REF_X = "ref_x";
        public static final String COLUMN_REF_Y = "ref_y";
        public static final String COLUMN_REF_LAT = "ref_lat";
        public static final String COLUMN_REF_LON = "ref_lon";
        public static final String COLUMN_ROTATE = "rot";
        public static final String COLUMN_SCALE = "scale";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_COORD_SYSTEM = "system";
        public static final String COLUMN_PROJECTION = "proj";
        public static final String COLUMN_P0 = "p0";
        public static final String COLUMN_M0 = "m0";
        public static final String COLUMN_X0 = "x0";
        public static final String COLUMN_Y0 = "y0";
        public static final String COLUMN_P1 = "p1";
        public static final String COLUMN_P2 = "p2";
        public static final String COLUMN_K0 = "k0";

    }
}
