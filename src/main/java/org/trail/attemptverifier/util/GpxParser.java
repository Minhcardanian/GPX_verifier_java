package org.trail.attemptverifier.util;

import org.springframework.stereotype.Component;
import org.trail.attemptverifier.model.TrackPoint;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust GPX Parser using DOM.
 *
 * Why DOM instead of regex?
 *  - Handles namespaces correctly
 *  - Properly handles nested tags
 *  - Rejects malformed XML early
 *  - Matches industrial GPX tool output
 *
 * Demonstrates:
 *  - Abstraction (service layer depends only on parse() result)
 *  - Encapsulation (implementation hidden)
 */
@Component
public class GpxParser {

    public List<TrackPoint> parse(InputStream inputStream) {
        List<TrackPoint> points = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList trkptNodes = doc.getElementsByTagName("trkpt");
            System.out.println("[GpxParser] DOM found " + trkptNodes.getLength() + " trkpt nodes.");

            for (int i = 0; i < trkptNodes.getLength(); i++) {
                Node node = trkptNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                Element el = (Element) node;

                // Latitude + Longitude (required by GPX spec)
                Double lat = parseDoubleAttr(el, "lat");
                Double lon = parseDoubleAttr(el, "lon");

                if (lat == null || lon == null) {
                    continue; // skip corrupted points
                }

                // Elevation (optional)
                Double ele = parseChildDouble(el, "ele");

                // Time (optional)
                Instant time = parseChildTime(el, "time");

                points.add(new TrackPoint(lat, lon, ele, time));
            }

        } catch (Exception e) {
            System.err.println("[GpxParser] ERROR parsing GPX via DOM: " + e.getMessage());
        }

        System.out.println("[GpxParser] Returning " + points.size() + " valid TrackPoint(s).");
        return points;
    }

    // -------------------------
    // Helpers (Encapsulation)
    // -------------------------

    private Double parseDoubleAttr(Element el, String attrName) {
        if (!el.hasAttribute(attrName)) return null;
        try {
            return Double.parseDouble(el.getAttribute(attrName));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Double parseChildDouble(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;

        String val = list.item(0).getTextContent().trim();
        try {
            return Double.parseDouble(val);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Instant parseChildTime(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;

        String val = list.item(0).getTextContent().trim();
        try {
            return Instant.parse(val);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
