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

        public static final int TYPE_LOCAL = 1;
        public static final int TYPE_GEOGRAPHIC = 2;
    }

    public static abstract class CoordSystems implements BaseColumns {

        public static final String CONTENT_PATH = "coord_systems";
        public static final String CONTENT_URI = BASE_URI + CONTENT_PATH;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chasm.coord_system";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.chasm.coord_system";

        public static final String TABLE = "coord_systems";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_PROJ = "proj";
        public static final String COLUMN_P0 = "p0";
        public static final String COLUMN_M0 = "m0";
        public static final String COLUMN_X0 = "x0";
        public static final String COLUMN_Y0 = "y0";
        public static final String COLUMN_P1 = "p1";
        public static final String COLUMN_P2 = "p2";
        public static final String COLUMN_SF = "sf";

        public static final int TYPE_UTM = 1;
        public static final int TYPE_SPCS = 2;
        public static final int TYPE_USER = 3;

        public static final int PROJ_TM = 1;    // Transverse Mercator
        public static final int PROJ_LC = 2;    // Lambert Conic
        public static final int PROJ_OM = 3;    // Oblique Mercator
        public static final int PROJ_OTHER = 0;
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
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_COORD_SYSTEM_CODE = "code";
        public static final String COLUMN_COORD_SYSTEM_TYPE = "type";
        public static final String COLUMN_COORD_SYSTEM_PROJ = "proj";
        public static final String COLUMN_P0 = "p0";
        public static final String COLUMN_M0 = "m0";
        public static final String COLUMN_X0 = "x0";
        public static final String COLUMN_Y0 = "y0";
        public static final String COLUMN_P1 = "p1";
        public static final String COLUMN_P2 = "p2";
        public static final String COLUMN_SF = "sf";

        public static final int UNITS_METER = 1;
        public static final int UNITS_SFT = 2;
    }
}
