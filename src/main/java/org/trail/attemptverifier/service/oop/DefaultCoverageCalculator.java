package org.trail.attemptverifier.service.oop;

import org.trail.attemptverifier.model.TrackPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of CoverageCalculator.
 *
 * Optimized for long GPX tracks:
 *  - Downsamples both attempt and route to a max number of points
 *  - Uses a sliding nearest-neighbour search along the route (O(N))
 */
public class DefaultCoverageCalculator implements CoverageCalculator {

    // Hard cap on effective points used in coverage computation
    private static final int MAX_POINTS = 5000;

    @Override
    public double computeCoverage(
            List<TrackPoint> attemptTrack,
            List<TrackPoint> routeTrack,
            double toleranceM
    ) {
        if (attemptTrack == null || attemptTrack.isEmpty()) return 0.0;
        if (routeTrack == null || routeTrack.isEmpty()) return 0.0;

        // Downsample big tracks so we don't explode runtime
        List<TrackPoint> attempt = downsample(attemptTrack, MAX_POINTS);
        List<TrackPoint> route   = downsample(routeTrack,   MAX_POINTS);

        int covered = 0;
        int total   = attempt.size();

        // Sliding index along route (we never go backwards)
        int j = 0;

        for (TrackPoint a : attempt) {
            // Move forward on the route while the next point is closer
            while (j + 1 < route.size()
                    && distanceMeters(route.get(j + 1), a) <= distanceMeters(route.get(j), a)) {
                j++;
            }

            double d = distanceMeters(route.get(j), a);
            if (d <= toleranceM) {
                covered++;
            }
        }

        if (total == 0) return 0.0;
        return covered / (double) total;
    }

    // --------------------------------------------------------
    // Helpers
    // --------------------------------------------------------

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

    // Haversine distance in meters
    private static double haversineMeters(double lat1Deg, double lon1Deg,
                                          double lat2Deg, double lon2Deg) {
        final double R = 6371000.0; // meters

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
        return R * c;
    }
}
