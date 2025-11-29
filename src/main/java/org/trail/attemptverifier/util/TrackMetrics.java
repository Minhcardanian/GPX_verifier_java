package org.trail.attemptverifier.util;

import org.trail.attemptverifier.model.TrackPoint;

import java.util.List;

public class TrackMetrics {

    // Earth radius in meters
    private static final double EARTH_RADIUS_M = 6371000.0;

    public static double computeTotalDistanceKm(List<TrackPoint> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }

        double totalMeters = 0.0;

        for (int i = 1; i < points.size(); i++) {
            TrackPoint p1 = points.get(i - 1);
            TrackPoint p2 = points.get(i);

            totalMeters += haversineMeters(
                    p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude()
            );
        }

        return totalMeters / 1000.0;
    }

    public static double computeElevationGainM(List<TrackPoint> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }

        double gain = 0.0;

        for (int i = 1; i < points.size(); i++) {
            TrackPoint p1 = points.get(i - 1);
            TrackPoint p2 = points.get(i);

            Double e1 = p1.getElevation();
            Double e2 = p2.getElevation();
            if (e1 == null || e2 == null) {
                continue;
            }

            double diff = e2 - e1;
            if (diff > 0) {
                gain += diff;
            }
        }

        return gain;
    }

    /**
     * Maximum deviation (in meters) between the attempt and the route:
     * For each attempt point, find the nearest route point and track the maximum nearest distance.
     */
    public static double computeMaxDeviationMeters(List<TrackPoint> attempt,
                                                   List<TrackPoint> route) {
        if (attempt == null || attempt.isEmpty() ||
                route == null || route.isEmpty()) {
            return Double.NaN;
        }

        double max = 0.0;

        for (TrackPoint a : attempt) {
            double min = Double.MAX_VALUE;

            for (TrackPoint r : route) {
                double d = haversineMeters(
                        a.getLatitude(), a.getLongitude(),
                        r.getLatitude(), r.getLongitude()
                );
                if (d < min) {
                    min = d;
                }
            }

            if (min > max) {
                max = min;
            }
        }

        return max;
    }

    /**
     * Coverage ratio: fraction of attempt points that are within toleranceMeters of the route.
     */
    public static double computeCoverageRatio(List<TrackPoint> attempt,
                                              List<TrackPoint> route,
                                              double toleranceMeters) {
        if (attempt == null || attempt.isEmpty() ||
                route == null || route.isEmpty()) {
            return 0.0;
        }

        int covered = 0;

        for (TrackPoint a : attempt) {
            double min = Double.MAX_VALUE;

            for (TrackPoint r : route) {
                double d = haversineMeters(
                        a.getLatitude(), a.getLongitude(),
                        r.getLatitude(), r.getLongitude()
                );
                if (d < min) {
                    min = d;
                }
            }

            if (min <= toleranceMeters) {
                covered++;
            }
        }

        return covered / (double) attempt.size();
    }

    // Haversine distance in meters
    private static double haversineMeters(double lat1Deg, double lon1Deg,
                                          double lat2Deg, double lon2Deg) {
        double lat1 = Math.toRadians(lat1Deg);
        double lon1 = Math.toRadians(lon1Deg);
        double lat2 = Math.toRadians(lat2Deg);
        double lon2 = Math.toRadians(lon2Deg);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
