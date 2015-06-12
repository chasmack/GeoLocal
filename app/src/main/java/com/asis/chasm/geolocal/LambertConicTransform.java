package com.asis.chasm.geolocal;

/**
 * Transform between a local basis and geographic coordinates using
 * a Lambert Conic projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class LambertConicTransform {

    private static TransformParams sParams;

    private LambertConicTransform() { }

    public static GeoPoint toGeographic(LocalPoint locPt, TransformParams params) {
        initTransform(params);
        return new GeoPoint(0.0, 0.0);
    }

    public static LocalPoint toLocal(GeoPoint geoPt, TransformParams params) {
        initTransform(params);
        return new LocalPoint(0.0, 0.0);
    }

    private static void initTransform(TransformParams params) {
        if (sParams.equals(params)) {
            return;
         }
        sParams = params;

        // init transform constants
    }
}
