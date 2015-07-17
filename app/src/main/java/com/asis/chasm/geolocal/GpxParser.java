package com.asis.chasm.geolocal;

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

    private static final String ns = null;

    // We don't use namespaces

    public List<Waypoint> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readGpx(parser);
        } finally {
            in.close();
        }
    }

    private List<Waypoint> readGpx(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Waypoint> entries = new ArrayList<Waypoint>();

        parser.require(XmlPullParser.START_TAG, ns, "gpx");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("wpt")) {
                entries.add(readWpt(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    public static class Waypoint {
        public final String name;
        public final double lat;
        public final double lon;
        public final String cmt;
        public final String desc;
        public final String samples;

        private Waypoint(String name, String lat, String lon, String cmt, String desc, String samples) {
            this.name = name;
            this.lat = Double.parseDouble(lat);
            this.lon = Double.parseDouble(lon);
            this.cmt = cmt;
            this.desc = desc;
            this.samples = samples;
        }

        @Override
        public String toString() {
            return String.format("wpt: %s lat/lon: %.6f, %.6f", name, lat, lon);
        }
    }

    // Parses the contents of a waypoint. If it encounters a name, etc. tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Waypoint readWpt(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "wpt");
        String lat = parser.getAttributeValue(null, "lat");
        String lon = parser.getAttributeValue(null, "lon");
        String name = null;
        String cmt = null;
        String desc = null;
        String samples = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (parser.getName()) {
                case "name":
                    name = readName(parser);
                    break;
                case "cmt":
                    cmt = readCmt(parser);
                    break;
                case "desc":
                    desc = readDesc(parser);
                    break;
                case "extensions":
                    samples = readExtensions(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        Waypoint wpt =  new Waypoint(name, lat, lon, cmt, desc, samples);
        return wpt;
    }

    // Processes name tags in the waypoint.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    // Processes cmt tags in the waypoint.
    private String readCmt(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "cmt");
        String cmt = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "cmt");
        return cmt;
    }

    // Processes desc tags in the waypoint.
    private String readDesc(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "desc");
        String desc = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "desc");
        return desc;
    }

    //    <extensions>
    //        <wptx1:WaypointExtension>
    //            <wptx1:Samples>2</wptx1:Samples>
    //        </wptx1:WaypointExtension>
    //    </extensions>
    // Processes extensions tags in the waypoint.
    private String readExtensions(XmlPullParser parser) throws IOException, XmlPullParserException {

        String samples = "8";
        skip(parser);

        //    parser.require(XmlPullParser.START_TAG, ns, "extensions");
        //    while (parser.next() != XmlPullParser.END_TAG) {
        //        if (parser.getEventType() != XmlPullParser.START_TAG) {
        //            continue;
        //        }
        //    }
        //    String samples = readText(parser);
        //    parser.require(XmlPullParser.END_TAG, ns, "extensions");

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
