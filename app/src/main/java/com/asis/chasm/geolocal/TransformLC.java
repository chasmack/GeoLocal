package com.asis.chasm.geolocal;

import android.util.Log;
import java.lang.Math;
import com.asis.chasm.geolocal.Settings.Params;

/**
 * Transform between a local basis and geographic coordinates using
 * a Lambert Conic projection as an intermediate basis.
 *
 * The local transform converts local x,y coordinates in local units
 * to grid coordinates in meters.  The grid coordinates are then converted
 * to geographic coordinates.
 */

public class TransformLC {

    private static final String TAG = "TransformLC";

    private static String sCode = "";

    // Coordinate system constants
    // private final static double A = 6378206.4;          // major radius of ellipsoid, meters (NAD27)
    // private final static double E = 0.08227185422;      // eccentricity of ellipsoid (NAD27)
    private static final double A = 6378137.0;             // major radius of ellipsoid, meters (NAD83)
    private static final double E = 0.08181922146;         // eccentricity of ellipsoid (NAD83)
    private static final double PI4 = Math.PI / 4.0;
    private static final double PI2 = Math.PI / 2.0;

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
    private static double F;
    private static double rho0;

    private TransformLC() { }

    public static GeoPoint toGeo(GridPoint p) {

        // Make sure the zone constants are initialized.
        initTransform();

        // Subtract the false northing/easting.
        double x = p.getX() - X0;
        double y = p.getY() - Y0;

        // Calculate the Longitude.
        double lon = Math.atan2(x, rho0 - y) / n + M0;

        // Estimate the Latitude.
        double rho = Math.sqrt(Math.pow(x, 2.0) + Math.pow(rho0 - y, 2.0));
        double t = Math.pow(rho / (A * F), 1.0 / n);
        double lat = PI2 - (2.0 * Math.atan2(t, 1.0));

        // Substitute the estimate into the iterative calculation
        // that converges on the correct Latitude value.
        double lat1;
        do {
            lat1 = lat ;
            double es = E * Math.sin(lat1);
            lat = PI2 - 2.0 * Math.atan2(t * Math.pow((1.0 - es) / (1.0 + es), E / 2.0), 1.0);
        } while (Math.abs (lat - lat1) > 2e-9);

        // Return lat/lon in degrees.
        return new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon));
    }

    public static GridPoint toGrid(GeoPoint p) {

        // Make sure the zone constants are initialized.
        initTransform();

        // Convert the lat/lon to grid coordinates.
        double lat = Math.toRadians(p.getLat());
        double lon = Math.toRadians(p.getLon());

        double m = Math.cos(lat) / Math.sqrt(1 - Math.pow(E * Math.sin(lat), 2));
        double t = Math.tan(PI4 - lat / 2.0)
                / Math.pow((1.0 - E * Math.sin(lat)) / (1.0 + E * Math.sin(lat)), E / 2.0);
        double rho = A * F * Math.pow(t, n);
        double theta = n * (lon - M0);
        double k = (rho * n) / (A * m);
        double x = rho * Math.sin(theta) + X0;
        double y = rho0 - rho * Math.cos(theta) + Y0;

        return new GridPoint(x, y).setTheta(Math.toDegrees(theta)).setK(k);
    }

    public static double getTheta(GeoPoint p) {

        // Make sure the zone constants are initialized.
        initTransform();

        double lon = Math.toRadians(p.getLon());
        return Math.toDegrees(n * (lon - M0));
    }

    public static double getK(GeoPoint p) {

        // Make sure the zone constants are initialized.
        initTransform();

        double lat = Math.toRadians(p.getLat());
        double t = Math.tan(PI4 - lat / 2.0)
                / Math.pow((1.0 - E * Math.sin(lat)) / (1.0 + E * Math.sin(lat)), E / 2.0);
        return A * F * Math.pow(t, n);
    }

    public static void initTransform() {

        // Check if zone constants need to be initialized.
        Params p = Params.getParams();
        if (sCode.equals(p.getProjectionCode())) { return; }

        // Check we are using a Lambert projection.
        if (p.getProjectionType() != PointsContract.Projections.TYPE_LC) {
            throw new IllegalArgumentException("Bad LC transform parameters.");
        }

        // Get the defining coordinate system constants for the zone
        P1 = Math.toRadians(p.getP1());
        P2 = Math.toRadians(p.getP2());
        P0 = Math.toRadians(p.getP0());
        M0 = Math.toRadians(p.getM0());
        Y0 = p.getY0();
        X0 = p.getX0();

        // Calculate the derived coordinate system constants.
        m1 = Math.cos(P1) / Math.sqrt(1.0 - Math.pow(E * Math.sin(P1), 2.0));
        m2 = Math.cos(P2) / Math.sqrt(1.0 - Math.pow(E * Math.sin(P2), 2.0));
        t1 = Math.tan(PI4 - P1 / 2.0)
                / Math.pow((1.0 - E * Math.sin(P1)) / (1.0 + E * Math.sin(P1)), E / 2.0);
        t2 = Math.tan(PI4 - P2 / 2.0)
                / Math.pow( (1.0 - E * Math.sin(P2)) / (1.0 + E * Math.sin(P2)), E / 2.0);
        t0 = Math.tan(PI4 - P0 / 2.0)
                / Math.pow((1.0 - E * Math.sin(P0)) / (1.0 + E * Math.sin(P0)), E / 2.0);
        n = Math.log(m1 / m2) / Math.log(t1 / t2);
        F = m1 / (n * Math.pow(t1, n));
        rho0 = A * F * Math.pow(t0, n);

        Log.d(TAG, "Transform initialized: " + p.getProjectionDesc()
                + " (" + p.getProjectionCode() + ")");
//        Log.d(TAG, "m1=" + m1);
//        Log.d(TAG, "m2=" + m2);
//        Log.d(TAG, "t1=" + t1);
//        Log.d(TAG, "t2=" + t2);
//        Log.d(TAG, "t0=" + t0);
//        Log.d(TAG, "n=" + n);
//        Log.d(TAG, "F=" + F);
//        Log.d(TAG, "rho0=" + rho0);

        // Projection constants have been initialized.
        sCode = p.getProjectionCode();
    }
}
