package org.trail.attemptverifier.service.oop;

/**
 * Abstraction for computing an attempt's difficulty score.
 * OOP: this is an interface so different scoring strategies
 * can be plugged in.
 */
public interface DifficultyModel {

    /**
     * Compute a single difficulty score from core metrics.
     *
     * @param distanceKm     total distance in kilometers
     * @param elevationGainM total positive elevation gain in meters
     * @param coverageRatio  0.0–1.0 fraction of attempt close to route
     * @param maxDeviationM  max deviation in meters from route
     * @return scalar difficulty score (non-negative)
     */
    double computeScore(
            double distanceKm,
            double elevationGainM,
            double coverageRatio,
            double maxDeviationM
    );
}
