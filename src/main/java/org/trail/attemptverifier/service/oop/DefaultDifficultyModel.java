package org.trail.attemptverifier.service.oop;

/**
 * Default concrete implementation of DifficultyModel.
 *
 * OOP:
 *  - Implements the DifficultyModel interface (polymorphism)
 *  - Encapsulates our current heuristic for scoring attempts
 */
public class DefaultDifficultyModel implements DifficultyModel {

    @Override
    public double computeScore(
            double distanceKm,
            double elevationGainM,
            double coverageRatio,
            double maxDeviationM
    ) {
        // Base load: distance + scaled elevation
        double base = distanceKm + (elevationGainM / 100.0);

        // Reward better coverage
        double coverageBonus = coverageRatio * 10.0;

        // Penalty for leaving the route
        double deviationPenalty;
        if (Double.isNaN(maxDeviationM)) {
            deviationPenalty = 10.0; // worst case if we couldn't compute deviation
        } else {
            deviationPenalty = Math.min(maxDeviationM / 50.0, 10.0);
        }

        double score = base + coverageBonus - deviationPenalty;
        return Math.max(score, 0.0);
    }
}
