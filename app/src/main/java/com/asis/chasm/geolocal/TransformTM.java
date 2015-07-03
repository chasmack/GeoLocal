package com.asis.chasm.geolocal;

import com.asis.chasm.geolocal.PointsContract.Transforms;

/**
 * Transform between a local basis and geographic coordinates using
 * a Transverse Mercator projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransformTM {

    private static TransformParams sParams;

    private TransformTM() { }

    public static GeoPoint toGeo(GridPoint pt, TransformParams params) {
        initTransform(params);
       return new GeoPoint(0.0, 0.0);
    }

    public static GridPoint toLocal(GeoPoint pt, TransformParams params) {
        initTransform(params);
        return new GridPoint(0.0, 0.0, Transforms.UNITS_METERS).setK(1.0).setTheta(0.0);
    }

    private static void initTransform(TransformParams params) {
        if (sParams.equals(params)) {
            return;
        }
        sParams = params;

        // init transform constants
    }
}
