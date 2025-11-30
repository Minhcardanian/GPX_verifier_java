package org.trail.attemptverifier.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.trail.attemptverifier.model.TrackPoint;
import org.trail.attemptverifier.util.GpxParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Loads the official route GPX from the classpath and caches the parsed TrackPoints.
 */
@Service
public class RouteService {

    private static final String OFFICIAL_ROUTE_PATH = "classpath:gpx/route_official.gpx";

    private final GpxParser gpxParser;
    private final ResourceLoader resourceLoader;

    private List<TrackPoint> cachedRoute;

    public RouteService(GpxParser gpxParser, ResourceLoader resourceLoader) {
        this.gpxParser = gpxParser;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Returns the official route TrackPoints. Parsed once and cached for subsequent calls.
     */
    public List<TrackPoint> getTrackPoints() {
        if (cachedRoute != null) {
            return cachedRoute;
        }

        Resource routeResource = resourceLoader.getResource(OFFICIAL_ROUTE_PATH);
        if (!routeResource.exists()) {
            System.err.println("[RouteService] Official route GPX not found at " + OFFICIAL_ROUTE_PATH);
            return Collections.emptyList();
        }

        try (InputStream in = routeResource.getInputStream()) {
            cachedRoute = gpxParser.parse(in);
            return cachedRoute;
        } catch (IOException e) {
            System.err.println("[RouteService] Failed to read official route: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
