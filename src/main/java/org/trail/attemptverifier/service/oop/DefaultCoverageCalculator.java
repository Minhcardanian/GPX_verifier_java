package org.trail.attemptverifier.service.oop;

import org.trail.attemptverifier.model.TrackPoint;
import java.util.List;

/**
 * Default implementation of CoverageCalculator.
 */
public class DefaultCoverageCalculator implements CoverageCalculator {

    @Override
    public double computeCoverage(
            List<TrackPoint> attemptTrack,
            List<TrackPoint> routeTrack,
            double toleranceM
    ) {
        if (attemptTrack == null || attemptTrack.isEmpty()) return 0.0;
        if (routeTrack == null || routeTrack.isEmpty()) return 0.0;

        int matched = 0;

        for (TrackPoint routePt : routeTrack) {

            boolean close = attemptTrack.stream().anyMatch(a ->
                    distanceMeters(
                            a.getLatitude(),  a.getLongitude(),
                            routePt.getLatitude(), routePt.getLongitude()
                    ) <= toleranceM
            );

            if (close) matched++;
        }

        return (double) matched / routeTrack.size();
    }

    /**
     * Haversine distance in meters.
     */
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
