package org.trail.attemptverifier.util;

import org.springframework.stereotype.Component;
import org.trail.attemptverifier.model.TrackPoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GpxParser {

    // Regex to match <trkpt ...> ... </trkpt>
    private static final Pattern TRKPT_PATTERN =
            Pattern.compile("<trkpt([^>]*)>(.*?)</trkpt>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern LAT_PATTERN =
            Pattern.compile("lat\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private static final Pattern LON_PATTERN =
            Pattern.compile("lon\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private static final Pattern ELE_PATTERN =
            Pattern.compile("<ele>(.*?)</ele>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern TIME_PATTERN =
            Pattern.compile("<time>(.*?)</time>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public List<TrackPoint> parse(InputStream inputStream) {
        List<TrackPoint> points = new ArrayList<>();

        try {
            // Read the whole file as UTF-8 text
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Matcher trkptMatcher = TRKPT_PATTERN.matcher(content);
            int count = 0;

            while (trkptMatcher.find()) {
                count++;

                String attrs = trkptMatcher.group(1);
                String inner = trkptMatcher.group(2);

                Double lat = extractDouble(attrs, LAT_PATTERN);
                Double lon = extractDouble(attrs, LON_PATTERN);

                if (lat == null || lon == null) {
                    continue; // skip invalid points
                }

                Double ele = extractDouble(inner, ELE_PATTERN);
                Instant time = extractInstant(inner, TIME_PATTERN);

                points.add(new TrackPoint(lat, lon, ele, time));
            }

            System.out.println("[GpxParser] Found " + count + " <trkpt> blocks, returning "
                    + points.size() + " valid points.");

        } catch (IOException e) {
            System.err.println("[GpxParser] WARNING: failed to read GPX input: " + e.getMessage());
        }

        return points;
    }

    private Double extractDouble(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Instant extractInstant(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            try {
                return Instant.parse(m.group(1).trim());
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
