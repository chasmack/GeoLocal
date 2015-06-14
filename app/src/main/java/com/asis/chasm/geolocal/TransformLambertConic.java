package com.asis.chasm.geolocal;

/**
 * Transform between a local basis and geographic coordinates using
 * a Lambert Conic projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransformLambertConic {

    private static TransformParams sParams;

    private TransformLambertConic() { }

    public static Point toGeographic(Point pt, TransformParams params) {
        initTransform(params);
        return new Point(Point.TYPE_GEOGRAPHIC);
    }

    public static Point toLocal(Point pt, TransformParams params) {
        initTransform(params);
        return new Point(Point.TYPE_LOCAL);
    }

    private static void initTransform(TransformParams params) {
        if (sParams.equals(params)) {
            return;
         }
        sParams = params;

        // init transform constants
    }
}
