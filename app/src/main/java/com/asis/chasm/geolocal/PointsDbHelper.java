package com.asis.chasm.geolocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.asis.chasm.geolocal.PointsContract.*;

/**
 * Created by Charlie on 6/3/2015.
 */

public class PointsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "points.db";
    public static final int DATABASE_VERSION = 1;

    private static final String COMMA_SEP = ", ";

    private static final String SQL_CREATE_POINTS =
            "CREATE TABLE " + Points.TABLE + " ("
                + Points._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Points.COLUMN_PT_NAME + " TEXT" + COMMA_SEP
                + Points.COLUMN_PT_DESC + " TEXT" + COMMA_SEP
                + Points.COLUMN_PT_TYPE + " INTEGER" + COMMA_SEP
                + Points.COLUMN_LOCAL_X + " REAL" + COMMA_SEP
                + Points.COLUMN_LOCAL_Y + " REAL" + COMMA_SEP
                + Points.COLUMN_LAT + " REAL" + COMMA_SEP
                + Points.COLUMN_LON + " REAL"
                + ")";

    private static final String SQL_CREATE_COORD_SYSTEMS =
            "CREATE TABLE " + Points.TABLE + " ("
                    + CoordSystems._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CoordSystems.COLUMN_CODE + " TEXT" + COMMA_SEP
                    + CoordSystems.COLUMN_DESC + " TEXT" + COMMA_SEP
                    + CoordSystems.COLUMN_TYPE + " INTEGER" + COMMA_SEP
                    + CoordSystems.COLUMN_PROJ + " INTEGER" + COMMA_SEP
                    + CoordSystems.COLUMN_P0 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_M0 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_X0 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_Y0 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_K0 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_P1 + " REAL" + COMMA_SEP
                    + CoordSystems.COLUMN_P2 + " REAL"
                    + ")";

    private static final String SQL_CREATE_TRANSFORM =
            "CREATE TABLE " + Points.TABLE + " ("
                    + Transform.COLUMN_REF_X + " REAL" + COMMA_SEP
                    + Transform.COLUMN_REF_Y + " REAL" + COMMA_SEP
                    + Transform.COLUMN_REF_LAT + " REAL" + COMMA_SEP
                    + Transform.COLUMN_REF_LON + " REAL" + COMMA_SEP
                    + Transform.COLUMN_ROTATE + " REAL" + COMMA_SEP
                    + Transform.COLUMN_SCALE + " REAL" + COMMA_SEP
                    + Transform.COLUMN_UNITS + " INTEGER" + COMMA_SEP
                    + Transform.COLUMN_COORD_SYSTEM_CODE + " TEXT" + COMMA_SEP
                    + Transform.COLUMN_COORD_SYSTEM_TYPE + " INTEGER" + COMMA_SEP
                    + Transform.COLUMN_COORD_SYSTEM_PROJ + " INTEGER" + COMMA_SEP
                    + Transform.COLUMN_P0 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_M0 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_X0 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_Y0 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_K0 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_P1 + " REAL" + COMMA_SEP
                    + Transform.COLUMN_P2 + " REAL"
                    + ")";

    private static final String SQL_DROP_POINTS =
            "DROP TABLE IF EXISTS " + Points.TABLE;

    private static final String SQL_DROP_COORD_SYSTEMS =
            "DROP TABLE IF EXISTS " + CoordSystems.TABLE;

    private static final String SQL_DROP_TRANSFORM =
            "DROP TABLE IF EXISTS " + Transform.TABLE;

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

