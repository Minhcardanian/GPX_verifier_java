package org.trail.attemptverifier.service.oop;

public abstract class DifficultyModel {

    public abstract double computeScore(
            double distanceKm,
            double elevationGainM,
            double coverageRatio,
            double maxDeviationM
    );

    protected double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
