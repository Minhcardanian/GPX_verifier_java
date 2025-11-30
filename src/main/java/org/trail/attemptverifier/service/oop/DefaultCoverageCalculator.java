package org.trail.attemptverifier.service.oop;

import org.trail.attemptverifier.util.GpxParser.TrackPoint;

import java.util.List;

public class DefaultCoverageCalculator implements CoverageCalculator {

    private static final double TOLERANCE_M = 25.0;

    @Override
    public double computeCoverage(List<TrackPoint> attempt, List<TrackPoint> official) {
        if (attempt.isEmpty() || official.isEmpty()) return 0.0;

        int matched = 0;

        for (TrackPoint off : official) {
            boolean close = attempt.stream()
                    .anyMatch(a -> haversine(a.lat(), a.lon(), off.lat(), off.lon()) * 1000 <= TOLERANCE_M);

            if (close) matched++;
        }

        return (double) matched / official.size();
    }

    @Override
    public double computeMaxDeviation(List<TrackPoint> attempt, List<TrackPoint> official) {
        double max = 0;

        for (TrackPoint a : attempt) {
            double minDist = official.stream()
                    .mapToDouble(o -> haversine(a.lat(), a.lon(), o.lat(), o.lon()) * 1000)
                    .min()
                    .orElse(9999);

            if (minDist > max) max = minDist;
        }

        return max;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2)*Math.sin(dLon/2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}
