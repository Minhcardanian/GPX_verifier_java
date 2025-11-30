package org.trail.attemptverifier.util;

import org.trail.attemptverifier.model.TrackPoint;

import java.util.List;

/**
 * TrackMetrics represents a computed summary of a list of TrackPoints.
 * This version follows OOP guidelines:
 *  - Encapsulated instance fields
 *  - Multiple constructors (overloading)
 *  - Aggregates related metrics into a domain object
 *  - Static utility methods still available for low-level math
 */
public class TrackMetrics {

    // ---- Instance fields ----
    private double distanceKm;
    private double elevationGainM;
    private double coverageRatio;      // 0.0 – 1.0
    private double maxDeviationM;      // meters

    // ---- Constructors ----

    /** Empty metrics (default values = 0) */
    public TrackMetrics() {
        this(0, 0, 0, 0);
    }

    /** Constructor used when only distance & elevation are known */
    public TrackMetrics(double distanceKm, double elevationGainM) {
        this(distanceKm, elevationGainM, 0, 0);
    }

    /** Full constructor */
    public TrackMetrics(double distanceKm,
                        double elevationGainM,
                        double coverageRatio,
                        double maxDeviationM) {
        this.distanceKm = distanceKm;
        this.elevationGainM = elevationGainM;
        this.coverageRatio = coverageRatio;
        this.maxDeviationM = maxDeviationM;
    }

    // ---- Getters & Setters ----

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getElevationGainM() {
        return elevationGainM;
    }

    public void setElevationGainM(double elevationGainM) {
        this.elevationGainM = elevationGainM;
    }

    public double getCoverageRatio() {
        return coverageRatio;
    }

    public void setCoverageRatio(double coverageRatio) {
        this.coverageRatio = coverageRatio;
    }

    public double getMaxDeviationM() {
        return maxDeviationM;
    }

    public void setMaxDeviationM(double maxDeviationM) {
        this.maxDeviationM = maxDeviationM;
    }

    // ---- Factory method to compute metrics from points ----

    public static TrackMetrics fromTracks(List<TrackPoint> attempt,
                                          List<TrackPoint> route,
                                          double toleranceMeters) {

        double distance = computeTotalDistanceKm(attempt);
        double gain = computeElevationGainM(attempt);

        double cov = computeCoverageRatio(attempt, route, toleranceMeters);
        double dev = computeMaxDeviationMeters(attempt, route);

        return new TrackMetrics(distance, gain, cov, dev);
    }

    // ---- Static helper methods ----

    private static final double EARTH_RADIUS_M = 6371000.0;

    public static double computeTotalDistanceKm(List<TrackPoint> points) {
        if (points == null || points.size() < 2) return 0.0;

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
        if (points == null || points.size() < 2) return 0.0;

        double gain = 0.0;
        for (int i = 1; i < points.size(); i++) {
            Double e1 = points.get(i - 1).getElevation();
            Double e2 = points.get(i).getElevation();
            if (e1 != null && e2 != null && e2 > e1) {
                gain += (e2 - e1);
            }
        }
        return gain;
    }

    public static double computeMaxDeviationMeters(List<TrackPoint> attempt,
                                                   List<TrackPoint> route) {
        if (attempt == null || attempt.isEmpty() ||
            route == null || route.isEmpty()) {
            return Double.NaN;
        }

        double max = 0;
        for (TrackPoint a : attempt) {
            double min = Double.MAX_VALUE;

            for (TrackPoint r : route) {
                double d = haversineMeters(
                        a.getLatitude(), a.getLongitude(),
                        r.getLatitude(), r.getLongitude()
                );

                if (d < min) min = d;
            }
            if (min > max) max = min;
        }

        return max;
    }

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
                if (d < min) min = d;
            }
            if (min <= toleranceMeters) covered++;
        }

        return covered / (double) attempt.size();
    }

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
