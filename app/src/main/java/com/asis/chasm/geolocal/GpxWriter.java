package com.asis.chasm.geolocal;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * GpxWriter
 */

public class GpxWriter {

    private final static String TAG = "GpxWriter";

    private final static String NAMESPACE_GPX = "http://www.topografix.com/GPX/1/1";
    private final static String NAMESPACE_GPXX = "http://www.garmin.com/xmlschemas/GpxExtensions/v3";
    private final static String NAMESPACE_WPTX1 = "http://www.garmin.com/xmlschemas/WaypointExtension/v1";
    private final static String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private final static String GPX_SCHEMA_LOCATION =
    "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd " +
    "http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd " +
    "http://www.garmin.com/xmlschemas/WaypointExtension/v1 http://www8.garmin.com/xmlschemas/WaypointExtensionv1.xsd";

    private final static String GPX_CREATOR = "com.asis.chasm.GeoLocal";
    private final static String GPX_VERSION = "1.1";

    public void write(Writer out, List<GpxParser.Waypoint> wpts)
            throws IOException, IllegalArgumentException, IllegalStateException {

        try {
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(new PrintWriter(out));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startDocument("utf-8", false);

            serializer.setPrefix("", NAMESPACE_GPX);
            serializer.setPrefix("gpxx", NAMESPACE_GPXX);
            serializer.setPrefix("wptx1", NAMESPACE_WPTX1);
            serializer.setPrefix("xsi", NAMESPACE_XSI);

            serializer.startTag(NAMESPACE_GPX, "gpx");
            serializer.attribute(null, "creator", GPX_CREATOR);
            serializer.attribute(null, "version", GPX_VERSION);
            serializer.attribute(NAMESPACE_XSI, "schemaLocation", GPX_SCHEMA_LOCATION);

            int cnt = 0;
            for (GpxParser.Waypoint wpt : wpts) {
                writeWpt(serializer, wpt);
                cnt++;
            }

            serializer.endTag(NAMESPACE_GPX, "gpx");
            serializer.endDocument();

            Log.d(TAG, "write waypoints cnt=" + cnt);

        } finally {
            out.close();
        }
    }

    private void writeWpt(XmlSerializer serializer, GpxParser.Waypoint wpt)
            throws IOException, IllegalArgumentException, IllegalStateException {

        serializer.startTag(null, "wpt");
        serializer.attribute(null, "lat", String.format("%.8f", wpt.lat));
        serializer.attribute(null, "lon", String.format("%.8f", wpt.lon));
        if (wpt.name != null) {
            serializer.startTag(null, "name");
            serializer.text(wpt.name);
            serializer.endTag(null, "name");
        }
        if (wpt.cmt != null) {
            serializer.startTag(null, "cmt");
            serializer.text(wpt.cmt);
            serializer.endTag(null, "cmt");
        }
        if (wpt.desc != null) {
            serializer.startTag(null, "desc");
            serializer.text(wpt.desc);
            serializer.endTag(null, "desc");
        }
        if (wpt.samples > 0) {
            serializer.startTag(null, "extensions");
            serializer.startTag(NAMESPACE_WPTX1, "WaypointExtension");
            serializer.startTag(NAMESPACE_WPTX1, "Samples");
            serializer.text(Integer.toString(wpt.samples));
            serializer.endTag(NAMESPACE_WPTX1, "Samples");
            serializer.endTag(NAMESPACE_WPTX1, "WaypointExtension");
            serializer.endTag(null, "extensions");
        }
        serializer.endTag(null, "wpt");
    }
}
