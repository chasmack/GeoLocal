package com.asis.chasm.geolocal;

import java.lang.Math;

/**
 * Transform between a local basis and geographic coordinates using
 * a Lambert Conic projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransformLC {


    private static TransformParams sParams;

    // Coordinate system constants
    // private final static double A = 6378206.4;          // major radius of ellipsoid, meters (NAD27)
    // private final static double E = 0.08227185422;      // eccentricity of ellipsoid (NAD27)
    private final static double A = 6378137.0;             // major radius of ellipsoid, meters (NAD83)
    private final static double E = 0.08181922146;         // eccentricity of ellipsoid (NAD83)
    private final static double PI4 = Math.PI / 4.0;
    private final static double PI2 = Math.PI / 2.0;

    // Defining coordinate system constants for the zone
    private static double P0;       // latitude of origin, radians (Bb)
    private static double M0;	    // central meridian, radians (Lo)
    private static double Y0;		// False northing of latitude of origin, meters (Nb)
    private static double X0;       // False easting of central meridian, meters (Eo)
    private static double P1;	    // latitude of first standard parallel, radians (Bs)
    private static double P2;	    // latitude of second standard parallel, radians (Bn)

    // Derived coordinate system constants for the zone
    private static double m1;
    private static double m2;
    private static double t1;
    private static double t2;
    private static double t0;
    private static double n;
    private static double  F;
    private static double rho0;

    private TransformLC() { }

    public static GeoPoint toGeo(GridPoint pt, TransformParams params) {

        // Initialize the zone constants if necessary.
        initTransform(params);

        // Calculate the Longitude.
        double x = pt.getX() - X0;
        double y = pt.getY() - Y0;

        double rho = Math.sqrt(Math.pow(x, 2.0) + Math.pow(rho0 - y, 2.0));
        double theta = Math.atan2(x, rho0 - y);
        double t = Math.pow(rho / (A * F), 1.0 / n);
        double lon = theta / n + M0;

        // Estimate the Latitude.
        double lat0 = PI2 - (2.0 * Math.atan2(t, 1.0));

        // Substitute the estimate into the iterative calculation
        // that converges on the correct Latitude value.
        double part1 = (1.0 - E * Math.sin(lat0)) / (1.0 + E * Math.sin(lat0));
        double lat1 = PI2 - 2.0 * Math.atan2(t * Math.pow(part1, E / 2.0), 1.0);
        do {
            lat0 = lat1 ;
            part1 = (1.0 - E * Math.sin(lat0)) / (1.0 + E * Math.sin(lat0));
            lat1 = PI2 - 2.0 * Math.atan2(t * Math.pow(part1, E / 2.0), 1.0);
        } while (Math.abs (lat1 - lat0) > 2e-9);

        // Return lat/lon in degrees.
        return new GeoPoint(Math.toDegrees(lat1), Math.toDegrees(lon));
    }

    public static GridPoint toGrid(double lat, double lon, TransformParams params) {
        initTransform(params);

        return new GridPoint(0.0, 0.0).setK(1.0).setTheta(0.0);
    }

    private static void initTransform(TransformParams params) {

        // Check if zone constants need to be initialized.
        if (sParams != null && sParams.equals(params)) return;
        sParams = params;

        // Get the defining coordinate system constants for the zone
        P1 = Math.toRadians(params.getP1());
        P2 = Math.toRadians(params.getP2());
        P0 = Math.toRadians(params.getP0());
        M0 = Math.toRadians(params.getM0());
        Y0 = params.getY0();
        X0 = params.getX0();

        // Calculate the derived coordinate system constants.
        m1 = Math.cos(P1) / Math.sqrt(1.0 - Math.pow(E, 2.0) * Math.pow(Math.sin(P1), 2.0));
        m2 = Math.cos(P2) / Math.sqrt(1.0 - Math.pow(E, 2.0) * Math.pow(Math.sin(P2), 2.0));
        t1 = Math.sin(PI4 - P1 / 2.0) / Math.cos(PI4 - P1 / 2.0) ;
        t1 = t1 / Math.pow( (1.0 - E * Math.sin(P1)) / (1.0 + E * Math.sin(P1)), E / 2.0);
        t2 = Math.sin(PI4 - P2 / 2.0) / Math.cos(PI4 - P2 / 2.0) ;
        t2 = t2 / Math.pow( (1.0 - E * Math.sin(P2)) / (1.0 + E * Math.sin(P2)), E / 2.0);
        t0 = Math.sin(PI4 - P0 / 2.0) / Math.cos(PI4 - P0 / 2.0) ;
        t0 = t0 / Math.pow( (1.0 - E * Math.sin(P0)) / (1.0 + E * Math.sin(P0)), E / 2.0);
        n = Math.log(m1 / m2) / Math.log(t1 / t2);
        F = m1 / (n * Math.pow(t1, n));
        rho0 = A * F * Math.pow(t0, n);
    }
}
