package org.trail.attemptverifier.service.oop;

import org.trail.attemptverifier.model.TrackPoint;
import java.util.List;

/**
 * Abstraction for coverage calculation.
 * OOP Principle: Interface defining behavior without implementation.
 */
public interface CoverageCalculator {

    /**
     * Computes the ratio of attempt points that lie within tolerance
     * of the official route.
     *
     * @param attemptTrack list of GPX points from user upload
     * @param routeTrack   list of points from official route GPX
     * @param toleranceM   max distance in meters considered “on route”
     * @return coverage ratio (0.0 – 1.0)
     */
    double computeCoverage(
            List<TrackPoint> attemptTrack,
            List<TrackPoint> routeTrack,
            double toleranceM
    );
}
