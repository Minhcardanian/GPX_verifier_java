package org.trail.attemptverifier.service.oop;

public class DefaultDifficultyModel extends DifficultyModel {

    @Override
    public double computeScore(double distanceKm,
                               double elevationGainM,
                               double coverageRatio,
                               double maxDeviationM) {

        double base = distanceKm * 1.2 + elevationGainM * 0.002;

        double penalty = 0;

        if (coverageRatio < 1.0) {
            penalty += (1.0 - coverageRatio) * 15;
        }

        if (maxDeviationM > 20) {
            penalty += (maxDeviationM - 20) * 0.05;
        }

        double score = base - penalty;
        return clamp(score, 0, 100);
    }
}
