package com.asis.chasm.geolocal;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 *  Sqlite contract class for the points database.
 */

public final class PointsContract {

    private PointsContract() {}

    public static final String AUTHORITY = "com.asis.chasm.geolocal.provider";

    public static abstract class Points implements BaseColumns {

        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("points")
                .build();

        public static final String TABLE = "points";
        public static final String COLUMN_PT_NAME = "name";
        public static final String COLUMN_PT_DESC = "desc";
        public static final String COLUMN_PT_TYPE = "type";
        public static final String COLUMN_LOCAL_X = "local_x";
        public static final String COLUMN_LOCAL_Y = "local_y";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";

        public static final int TYPE_LOCAL = 1;
        public static final int TYPE_GEOGRAPHIC = 2;

    }

    public static abstract class CoordSystems implements BaseColumns {

        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("coord_systems")
                .build();

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

        public static final int PROJ_TM = 1;
        public static final int PROJ_LCC = 2;
    }

    public static abstract class Transform {

        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("transform")
                .build();

        public static final String TABLE = "transform";
        public static final String COLUMN_REF_X = "ref_x";
        public static final String COLUMN_REF_Y = "ref_y";
        public static final String COLUMN_REF_LAT = "ref_lat";
        public static final String COLUMN_REF_LON = "ref_lon";
        public static final String COLUMN_ROTATE = "rot";
        public static final String COLUMN_SCALE = "sf";
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
