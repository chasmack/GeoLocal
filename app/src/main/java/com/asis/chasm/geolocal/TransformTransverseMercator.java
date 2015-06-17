package com.asis.chasm.geolocal;

/**
 * Transform between a local basis and geographic coordinates using
 * a Transverse Mercator projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransformTransverseMercator {

    private static TransformParams sParams;

    private TransformTransverseMercator() { }

    public static void toGeographic(Point pt, TransformParams params) {
        initTransform(params);
        pt.setLatLon(0.0, 0.0);
    }

    public static void toLocal(Point pt, TransformParams params) {
        initTransform(params);
        pt.setXY(0.0, 0.0);
    }

    private static void initTransform(TransformParams params) {
        if (sParams.equals(params)) {
            return;
        }
        sParams = params;

        // init transform constants
    }
}
