package org.trail.attemptverifier.service.oop;

import org.trail.attemptverifier.util.GpxParser.TrackPoint;

import java.util.List;

public interface CoverageCalculator {

    double computeCoverage(
            List<TrackPoint> attempt,
            List<TrackPoint> official
    );

    double computeMaxDeviation(
            List<TrackPoint> attempt,
            List<TrackPoint> official
    );
}
