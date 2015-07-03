package com.asis.chasm.geolocal;

import android.util.Log;

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

    private static final String TAG = "TransformTM";

    // Coordinate system constants
    private static final double a = 6378137.0;          // semi-major radius of ellipsoid, meters (WGS84)
    // private static final double f = 1 / 298.257223563;  // flattening (WGS84)
    private static final double f = 1 / 298.25722210088;  // flattening (GRS80)
    private static final double e2 = 2*f - f*f;         // first eccentricity squared
    private static final double e12 = e2 / (1 - e2);    // second eccentricity squared
    private static final double n = f / (2 - f);
    private static final double n2 = n  * n;
    private static final double n3 = n2 * n;
    private static final double n4 = n3 * n;
    private static final double r = a * (1 - n) * (1 - n2) * (1 + 9 * n2 / 4 + 225 * n4 / 64);

    // Defining coordinate system constants for the zone
    private static double P0;       // Latitude of origin, radians (Bb)
    private static double M0;	    // Central meridian, radians (Lo)
    private static double Y0;		// False northing of latitude of origin, meters (No)
    private static double X0;       // False easting of central meridian, meters (Eo)
    private static double K0;	    // Grid scale factor assigned to central meridian

    private static double u0, u2, u4, u6, u8;
    private static double v0, v2, v4, v6, v8;
    private static double s0;

    private static TransformParams sParams;

    private TransformTM() { }

    // Inverse calculation from grid coordinates to lat/lon.
    public static GeoPoint toGeo(GridPoint p, TransformParams xp) {

        // Check that the zone constants are initialized.
        initTransform(xp);

        double x = p.getX();
        double y = p.getY();

        double omega = (y - Y0 + s0) / (K0 * r);
        double sino = Math.sin(omega);
        double coso = Math.cos(omega);
        double coso2 = coso * coso;

        double pf = omega + sino * coso * (v0 + coso2 * (v2 + coso2 * (v4 + v6 * coso2)));

        double sinf = Math.sin(pf);
        double sinf2 = sinf * sinf;
        double cosf = Math.cos(pf);
        double cosf2 = cosf * cosf;
        double tanf = Math.tan(pf);
        double tanf2 = tanf * tanf;
        double tanf4 = tanf2 * tanf2;
        double tanf6 = tanf4 * tanf2;
        double etf2 = e12 + cosf2;
        double etf4 = etf2 * etf2;

        double Q = (x - X0) / (K0 * a / Math.sqrt(1 - e2 * sinf2));
        double Q2 = Q * Q;

        double b2 = tanf * ( 1 + etf2) / -2;
        double b4 = (5 + 3 * tanf2 + etf2 * (1 - 9 * tanf2) - 4 * etf4) / -12;
        double b6 = (61 + 90 * tanf2 + 45 * tanf4 + etf2 * (46 - 252 * tanf2 - 90 * tanf4)) / 360;

        double lat = pf + b2 * Q2 * (1 + Q2 * (b4 + b6 * Q2));

        double b3 = (1 + 2 * tanf2 + etf2) / -6;
        double b5 = (5 + 28 * tanf2 + 24 * tanf4 + etf2 * (6 + 8 * tanf2)) / 120;
        double b7 = (61 + 662 * tanf2 + 1320 * tanf4 + 720 * tanf6) / -5040;

        double lon = Q * (1 + Q2 * (b3 + Q2 * (b5 + b7 * Q2)));

        return new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon));
    }

    /**
     * Forward calculations of grid coordinates (northing/easting = y/x),
     * convergence angle (theta) and grid scale factor (k) from geographic
     *coordinates (lat/lon).  Grid coordinates are in meters.
    */
    public static GridPoint toGrid(GeoPoint p, TransformParams xp) {

        // Check that the zone constants are initialized.
        initTransform(xp);

        double lat = Math.toRadians(p.getLat());
        double lon = Math.toRadians(p.getLon());

        double L = (M0 - lon) * Math.cos(lat);
        double L2 = L * L;

        double sinp = Math.sin(lat);
        double sinp2 = sinp * sinp;
        double cosp = Math.cos(lat);
        double cosp2 = cosp * cosp;

        double s = K0 * (lat + sinp * cosp * (u0 + cosp2 * (u2 + cosp2 * (u4 + u6 * cosp2)))) * r;
        double R = K0 * a / Math.sqrt(1 - e2 * sinp2);

        double tanp = Math.tan(lat);
        double tanp2 = tanp * tanp;
        double tanp4 = tanp2 * tanp2;
        double tanp6 = tanp4 * tanp2;
        double et2 = e12 + cosp2;
        double et4 = et2 * et2;

        double a2 = R * tanp / 2;
        double a4 = (5 - tanp2 + et2 * (9 + 4 * et2)) / 12;
        double a6 = (61 - 58 * tanp2 + tanp2 * tanp2 + et2 * (270 - 330 * tanp2)) / 360;
        double y  = s - s0 + Y0 + a2 * L2 * (1 + L2 * (a4 + a6 * L2));

        double a1 = -1 * R;
        double a3 = (1 - tanp2 + et2) / 6;
        double a5 = (5 - 18 * tanp2 + tanp4 + et2 * (14 - 58 * tanp2)) / 120;
        double a7 = (61 - 479 * tanp2 + 179 * tanp4 - tanp6) / 5040;
        double x  = X0 + a1 * L * (1 + L2 * (a3 + L2 * (a5 + a7 * L2)));

        double c1 = -1 * tanp;
        double c3 = (1 + 3 * et2 + 2 * et4) / 3;
        double c5 = (2 - tanp2) / 15;
        double theta = c1 * L * (1 + L2 * (c3 + c5 * L2));

        double f2 = (1 + et2) / 2;
        double f4 = (5 - 4 * tanp2 + et2 * (9 - 24 * tanp2)) / 12;
        double k = K0 * (1 + f2 * L2 * (1 + f4 * L2));

        return new GridPoint(x, y)
                .setK(k)
                .setTheta(Math.toDegrees(theta));
    }

    public static void initTransform(TransformParams xp) {

        // Check if zone constants need to be initialized.
        if (sParams != null && sParams.equals(xp)) { return; }

        // Check we are using a Lambert projection.
        if (xp.getProjection() != PointsContract.Projections.PROJECTION_TM) {
            throw new IllegalArgumentException("Bad TM transform parameters.");
        }
        sParams = xp;

        // Get the defining coordinate system constants for the zone
        P0 = Math.toRadians(xp.getP0());
        M0 = Math.toRadians(xp.getM0());
        Y0 = xp.getY0();
        X0 = xp.getX0();
        K0 = xp.getK0();

        // Initialize the zone constants.
        u2 = -3 * n / 2 + 9 * n3 / 16;
        u4 = 15 * n2 / 16 - 15 * n4 / 32;
        u6 = -35 * n3 / 48;
        u8 = 315 * n4 / 512;

        u0 = 2 * (u2 - 2 * u4 + 3 * u6 - 4 * u8);
        u2 = 8 * (u4 - 4 * u6 + 10 * u8);
        u4 = 32 * (u6 - 6 * u8);
        u6 = 128 * u8;

        v2 = 3 * n / 2 - 27 * n3 / 32;
        v4 = 21 * n2 / 16 - 55 * n4 / 32;
        v6 = 151 * n3 / 96;
        v8 = 1097 * n4 / 512;

        v0 = 2 * (v2 - 2 * v4 + 3 * v6 - 4 * v8);
        v2 = 8 * (v4 - 4 * v6 + 10 * v8);
        v4 = 32 * (v6 - 6 * v8);
        v6 = 128 * v8;

        double sinp = Math.sin(P0);
        double cosp = Math.cos(P0);
        double cosp2 = cosp * cosp;
        s0 = K0 * (P0 + sinp * cosp * (u0 + cosp2 * (u2 + cosp2 * (u4 + u6 * cosp2)))) * r;

        Log.d(TAG, "Transform initialized.");
        Log.d(TAG, " n=" + n);
        Log.d(TAG, " r=" + r);
        Log.d(TAG, "u0=" + u0);
        Log.d(TAG, "u2=" + u2);
        Log.d(TAG, "u4=" + u4);
        Log.d(TAG, "u6=" + u6);
        Log.d(TAG, "v0=" + v0);
        Log.d(TAG, "v2=" + v2);
        Log.d(TAG, "v4=" + v4);
        Log.d(TAG, "v6=" + v6);
        Log.d(TAG, "s0=" + s0);
    }
}
