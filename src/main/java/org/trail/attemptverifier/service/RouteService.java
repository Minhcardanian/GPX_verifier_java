package org.trail.attemptverifier.service;

import org.springframework.stereotype.Service;
import org.trail.attemptverifier.model.Route;
import org.trail.attemptverifier.model.TrackPoint;
import org.trail.attemptverifier.util.GpxParser;
import org.trail.attemptverifier.util.TrackMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Loads and exposes the official route used for verification.
 *
 * Responsibilities:
 *  - Load GPX route from classpath
 *  - Parse into TrackPoint list
 *  - Precompute metrics (distance, elevation)
 *  - Provide clean accessors for services
 *
 * Demonstrates OOP:
 *  - Encapsulation (route stored privately)
 *  - Abstraction (services use getRouteTrack(), not GPX logic)
 *  - Extensibility (future multiple routes or inheritance via Route subclasses)
 */
@Service
public class RouteService {

    private final Route route;
    private final TrackMetrics routeMetrics;

    public RouteService(GpxParser gpxParser) {
        Route loadedRoute = null;
        TrackMetrics metrics = null;

        try (InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("gpx/route_official.gpx")) {

            if (in == null) {
                System.err.println("[RouteService] ERROR: Missing resource gpx/route_official.gpx");
            } else {
                try {
                    List<TrackPoint> points = gpxParser.parse(in);

                    if (points == null || points.isEmpty()) {
                        System.err.println("[RouteService] WARNING: Route GPX parsed but contains no points.");
                    } else {
                        loadedRoute = new Route("Official Test Route", points);

                        metrics = TrackMetrics.fromTracks(
                                points,
                                null,          // route = null because this is the route itself
                                0.0            // tolerance not needed
                        );

                        System.out.println("[RouteService] Loaded route: "
                                + points.size() + " points, "
                                + metrics.getDistanceKm() + " km, "
                                + metrics.getElevationGainM() + " m gain.");
                    }

                } catch (Exception e) {
                    System.err.println("[RouteService] GPX parse EXCEPTION: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[RouteService] IO error loading GPX: " + e.getMessage());
        }

        this.route = loadedRoute;
        this.routeMetrics = metrics;
    }

    // -----------------------
    // Public API
    // -----------------------

    /** Full route metadata object */
    public Route getRoute() {
        return route;
    }

    /** Returns raw TrackPoint list */
    public List<TrackPoint> getRouteTrack() {
        return (route != null) ? route.getTrackPoints() : Collections.emptyList();
    }

    /** Route distance in km */
    public double getRouteDistanceKm() {
        return (routeMetrics != null) ? routeMetrics.getDistanceKm() : 0.0;
    }

    /** Route total ascent */
    public double getRouteElevationGainM() {
        return (routeMetrics != null) ? routeMetrics.getElevationGainM() : 0.0;
    }

    /** Access all precomputed metrics */
    public TrackMetrics getRouteMetrics() {
        return routeMetrics;
    }
}
