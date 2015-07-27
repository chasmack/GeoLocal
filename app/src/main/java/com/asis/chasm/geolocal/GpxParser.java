package com.asis.chasm.geolocal;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GpxParser - parse GPX into Waypoints.
 */
public class GpxParser {

    private final static String TAG = "GpxParser";

    //    <gpx creator="GPSMAP 64" version="1.1"
    //        xmlns="http://www.topografix.com/GPX/1/1"
    //        xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3"
    //        xmlns:wptx1="http://www.garmin.com/xmlschemas/WaypointExtension/v1"
    //        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    //        xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd
    //            http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd
    //            http://www.garmin.com/xmlschemas/WaypointExtension/v1 http://www8.garmin.com/xmlschemas/WaypointExtensionv1.xsd" >
    //
    //        <metadata>
    //            <lat href="http://www.garmin.com">
    //                <text>Garmin International</text>
    //            </lat>
    //            <time>2015-04-27T23:09:11Z</time>
    //        </metadata>
    //
    //        <wpt lat="41.097316" lon="-123.696170">
    //            <ele>107.753052</ele>
    //            <time>2015-04-27T23:33:44Z</time>
    //            <name>4501</name>
    //            <cmt>SW212</cmt>
    //            <desc>27-APR-15 15:33:44</desc>
    //            <sym>Flag, Blue</sym>
    //            <extensions>
    //                <wptx1:WaypointExtension>
    //                    <wptx1:Samples>2</wptx1:Samples>
    //                </wptx1:WaypointExtension>
    //            </extensions>
    //        </wpt>
    //    </gpx>

    // Namespaces
    private final static String NAMESPACE_GPX = "http://www.topografix.com/GPX/1/1";
    private final static String NAMESPACE_GPXX = "http://www.garmin.com/xmlschemas/GpxExtensions/v3";
    private final static String NAMESPACE_WPTX1 = "http://www.garmin.com/xmlschemas/WaypointExtension/v1";

    public List<Waypoint> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(in, null);
            parser.nextTag();
            return readGpx(parser);

        } finally {
            in.close();
        }
    }

    private List<Waypoint> readGpx(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<Waypoint> waypoints = new ArrayList<Waypoint>();

        parser.require(XmlPullParser.START_TAG, null, "gpx");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            // Find first level elements and call their parsing routines.
            switch (parser.getName()) {
                case "metadata":
                    skip(parser);
                    break;
                case "wpt":
                    waypoints.add(readWpt(parser));
                    break;
                case "rte":
                    skip(parser);
                    break;
                case "trk":
                    skip(parser);
                    break;
                case "extensions":
                    skip(parser);
                    break;
                default:
                    skip(parser);
            }
        }
        return waypoints;
    }

    public static class Waypoint {
        public final String name;
        public final double lat;
        public final double lon;
        public final String desc;
        public final String cmt;
        public final String time;
        public final String symbol;
        public final int samples;

        public Waypoint(String name, double lat, double lon, String desc,
                        String cmt, String time, String symbol, int samples) {

            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.desc = desc;
            this.cmt = cmt;
            this.time = time;
            this.symbol = symbol;
            this.samples = samples;
        }

        @Override
        public String toString() {
            return String.format("wpt name=%s desc=%s cmt=%s lat/lon=%.6f/%.6f%s",
                    (name != null ? name : "<null>"),
                    (desc != null ? desc : "<null>"),
                    (cmt != null ? cmt : "<null>"),
                    lat, lon,
                    (samples > 0 ? ", samples:" + Integer.toString(samples) : ""));
        }
    }

    // Parses the contents of a waypoint. If it encounters a name, etc. tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Waypoint readWpt(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "wpt");
        double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
        double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));

        String name = null;
        String desc = null;
        String cmt = null;
        String time = null;
        String sym = null;
        int samples = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (parser.getName()) {
                case "ele":
                    skip(parser);
                    break;
                case "time":
                    time = readTime(parser);
                    break;
                case "name":
                    name = readName(parser);
                    break;
                case "cmt":
                    cmt = readCmt(parser);
                    break;
                case "desc":
                    desc = readDesc(parser);
                    break;
                case "sym":
                    sym = readSym(parser);
                    break;
                case "extensions":
                    samples = readExtensions(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        Waypoint wpt =  new Waypoint(name, lat, lon, desc, cmt, time, sym, samples);
        return wpt;
    }

    // Processes name tags in the waypoint.
    private String readTime(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "time");
        String time = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "time");
        return time;
    }

    // Processes name tags in the waypoint.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return name;
    }

    // Processes cmt tags in the waypoint.
    private String readCmt(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "cmt");
        String cmt = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "cmt");
        return cmt;
    }

    // Processes desc tags in the waypoint.
    private String readDesc(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "desc");
        String desc = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "desc");
        return desc;
    }

    // Processes sym tags in the waypoint.
    private String readSym(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "sym");
        String desc = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "sym");
        return desc;
    }

    //    <extensions>
    //        <gpxx:WaypointExtension>
    //            <gpxx:DisplayMode>SymbolAndName</gpxx:DisplayMode>
    //        </gpxx:WaypointExtension>
    //        <wptx1:WaypointExtension>
    //            <wptx1:DisplayMode>SymbolAndName</wptx1:DisplayMode>
    //            <wptx1:Samples>6</wptx1:Samples>
    //        </wptx1:WaypointExtension>
    //        <ctx:CreationTimeExtension>
    //            <ctx:CreationTime>2015-04-01T22:15:03Z</ctx:CreationTime>
    //        </ctx:CreationTimeExtension>
    //    </extensions>

    // Processes extensions tags in the waypoint.
    private int readExtensions(XmlPullParser parser) throws IOException, XmlPullParserException {

        int samples = 0;
        Log.d(TAG, "readExtensions...");
        parser.require(XmlPullParser.START_TAG, null, "extensions");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            Log.d(TAG, "parse START_TAG " + parser.getPrefix() + ":" + parser.getName());
            if (parser.getNamespace().equals(NAMESPACE_WPTX1)) {
                parser.require(XmlPullParser.START_TAG, NAMESPACE_WPTX1, "WaypointExtension");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }

                    Log.d(TAG, "parse START_TAG " + parser.getPrefix() + ":" + parser.getName());
                    if (parser.getName().equals("Samples")) {
                        samples = Integer.parseInt(readText(parser));

                    } else {
                        skip(parser);
                        Log.d(TAG, "skip END_TAG " + parser.getPrefix() + ":" + parser.getName());
                    }
                }

            } else {
                skip(parser);
                Log.d(TAG, "skip END_TAG " + parser.getPrefix() + ":" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "extensions");
        return samples;
    }

    // For the tags name, etc. extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
