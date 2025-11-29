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

@Service
public class RouteService {

    private final Route route;
    private final double routeDistanceKm;
    private final double routeElevationGainM;

    public RouteService(GpxParser gpxParser) {
        Route loadedRoute = null;
        double distanceKm = 0.0;
        double elevationGainM = 0.0;

        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("gpx/route_official.gpx")) {

            if (in == null) {
                System.err.println("[RouteService] WARNING: gpx/route_official.gpx not found on classpath.");
            } else {
                try {
                    List<TrackPoint> trackPoints = gpxParser.parse(in);
                    if (trackPoints.isEmpty()) {
                        System.err.println("[RouteService] WARNING: official route GPX has no track points.");
                    } else {
                        loadedRoute = new Route("Official Test Route", trackPoints);
                        distanceKm = TrackMetrics.computeTotalDistanceKm(trackPoints);
                        elevationGainM = TrackMetrics.computeElevationGainM(trackPoints);
                        System.out.println("[RouteService] Loaded official route with " +
                                trackPoints.size() + " points, distance " +
                                distanceKm + " km, elevation gain " +
                                elevationGainM + " m.");
                    }
                } catch (RuntimeException e) {
                    System.err.println("[RouteService] WARNING: failed to parse official route GPX: "
                            + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[RouteService] WARNING: failed to load official route GPX: " + e.getMessage());
        }

        this.route = loadedRoute;
        this.routeDistanceKm = distanceKm;
        this.routeElevationGainM = elevationGainM;
    }

    public Route getRoute() {
        return route;
    }

    public List<TrackPoint> getTrackPoints() {
        return route != null ? route.getTrackPoints() : Collections.emptyList();
    }

    public double getRouteDistanceKm() {
        return routeDistanceKm;
    }

    public double getRouteElevationGainM() {
        return routeElevationGainM;
    }
}
