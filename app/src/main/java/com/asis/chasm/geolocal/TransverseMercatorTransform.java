package com.asis.chasm.geolocal;

/**
 * Transform between a local basis and geographic coordinates using
 * a Transverse Mercator projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransverseMercatorTransform {

    private static TransformParams sParams;

    private TransverseMercatorTransform() { }

    public static GeoPoint toGeographic(LocalPoint p, TransformParams params) {
        if (sParams != params) {
            sParams = params;
            // init constants
        }
        return new GeoPoint(0.0, 0.0);
    }

    public static LocalPoint toLocal(GeoPoint pnt, TransformParams params) {
        if (sParams != params) {
            sParams = params;
            // init constants
        }
        return new LocalPoint(0.0, 0.0);
    }
}
