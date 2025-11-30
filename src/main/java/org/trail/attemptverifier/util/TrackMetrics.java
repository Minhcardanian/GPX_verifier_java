package org.trail.attemptverifier.util;

import org.trail.attemptverifier.model.TrackPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * TrackMetrics represents a computed summary of a list of TrackPoints.
 * This version follows OOP guidelines and includes optimized
 * coverage/deviation computations suitable for long GPX tracks.
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
    private static final int MAX_POINTS = 5000;

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

    /**
     * Approximate maximum deviation between attempt and route.
     * Uses downsampling + sliding nearest-neighbour search (O(N)).
     */
    public static double computeMaxDeviationMeters(List<TrackPoint> attempt,
                                                   List<TrackPoint> route) {
        if (attempt == null || attempt.isEmpty() ||
            route == null || route.isEmpty()) {
            return Double.NaN;
        }

        List<TrackPoint> aList = downsample(attempt, MAX_POINTS);
        List<TrackPoint> rList = downsample(route,   MAX_POINTS);

        double max = 0.0;
        int j = 0;

        for (TrackPoint a : aList) {
            while (j + 1 < rList.size()
                    && distanceMeters(rList.get(j + 1), a) <= distanceMeters(rList.get(j), a)) {
                j++;
            }

            double d = distanceMeters(rList.get(j), a);
            if (d > max) {
                max = d;
            }
        }

        return max;
    }

    /**
     * Coverage ratio (0.0–1.0) of attempt points that lie within
     * toleranceMeters of some point on the route. Also uses
     * downsampling + sliding nearest-neighbour search (O(N)).
     */
    public static double computeCoverageRatio(List<TrackPoint> attempt,
                                              List<TrackPoint> route,
                                              double toleranceMeters) {
        if (attempt == null || attempt.isEmpty() ||
            route == null || route.isEmpty()) {
            return 0.0;
        }

        List<TrackPoint> aList = downsample(attempt, MAX_POINTS);
        List<TrackPoint> rList = downsample(route,   MAX_POINTS);

        int covered = 0;
        int total   = aList.size();
        int j = 0;

        for (TrackPoint a : aList) {
            while (j + 1 < rList.size()
                    && distanceMeters(rList.get(j + 1), a) <= distanceMeters(rList.get(j), a)) {
                j++;
            }

            double d = distanceMeters(rList.get(j), a);
            if (d <= toleranceMeters) {
                covered++;
            }
        }

        if (total == 0) return 0.0;
        return covered / (double) total;
    }

    // ---- Low-level helpers ----

    private static List<TrackPoint> downsample(List<TrackPoint> src, int maxPoints) {
        int n = src.size();
        if (n <= maxPoints) {
            return src;
        }
        List<TrackPoint> out = new ArrayList<>(maxPoints);
        double step = (double) n / maxPoints;
        double idx = 0.0;
        for (int i = 0; i < maxPoints; i++) {
            int pos = (int) Math.round(idx);
            if (pos >= n) pos = n - 1;
            out.add(src.get(pos));
            idx += step;
        }
        return out;
    }

    private static double distanceMeters(TrackPoint p1, TrackPoint p2) {
        return haversineMeters(
                p1.getLatitude(), p1.getLongitude(),
                p2.getLatitude(), p2.getLongitude()
        );
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
