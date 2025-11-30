package org.trail.attemptverifier.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.trail.attemptverifier.model.Attempt;
import org.trail.attemptverifier.model.TrackPoint;
import org.trail.attemptverifier.repository.AttemptRepository;
import org.trail.attemptverifier.service.oop.CoverageCalculator;
import org.trail.attemptverifier.service.oop.DifficultyModel;
import org.trail.attemptverifier.service.oop.DefaultCoverageCalculator;
import org.trail.attemptverifier.service.oop.DefaultDifficultyModel;
import org.trail.attemptverifier.util.GpxParser;
import org.trail.attemptverifier.util.TrackMetrics;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core business logic for verifying runner GPX attempts.
 * Demonstrates clean OOP:
 *  - Encapsulation (service owns verification pipeline)
 *  - Polymorphism (DifficultyModel & CoverageCalculator interfaces)
 *  - Separation of concerns (parser, metrics, scoring, classification)
 */
@Service
public class AttemptVerifierService {

    private final AttemptRepository attemptRepository;
    private final GpxParser gpxParser;
    private final RouteService routeService;

    // OOP strategy implementations
    private final DifficultyModel difficultyModel = new DefaultDifficultyModel();
    private final CoverageCalculator coverageCalculator = new DefaultCoverageCalculator();

    private static final double COVERAGE_TOLERANCE_M = 30.0;

    public AttemptVerifierService(AttemptRepository attemptRepository,
                                  GpxParser gpxParser,
                                  RouteService routeService) {

        this.attemptRepository = attemptRepository;
        this.gpxParser = gpxParser;
        this.routeService = routeService;
    }

    /**
     * Verification pipeline:
     * 1) Parse GPX
     * 2) Load official route
     * 3) Compute metrics
     * 4) Score attempt
     * 5) Classify
     * 6) Persist to DB (including raw GPX bytes)
     */
    public Attempt verifyAttempt(MultipartFile gpxFile, String runnerId) throws IOException {

        // ---------------------------------------
        // Step 1 — Parse runner GPX
        // ---------------------------------------
        List<TrackPoint> attemptTrack;
        try {
            attemptTrack = gpxParser.parse(gpxFile.getInputStream());
        } catch (Exception e) {
            System.err.println("[AttemptVerifierService] GPX parse error: " + e.getMessage());
            return buildRejectedAttempt(runnerId, "Invalid GPX content.");
        }

        if (attemptTrack.isEmpty()) {
            return buildRejectedAttempt(runnerId, "No valid track points found.");
        }

        // ---------------------------------------
        // Step 2 — Load official route track
        // ---------------------------------------
        List<TrackPoint> route = routeService.getTrackPoints();
        if (route == null || route.isEmpty()) {
            return buildRejectedAttempt(runnerId, "Official route not available.");
        }

        // ---------------------------------------
        // Step 3 — Compute metrics using existing TrackMetrics API
        // ---------------------------------------
        double distanceKm = TrackMetrics.computeTotalDistanceKm(attemptTrack);
        double elevationGainM = TrackMetrics.computeElevationGainM(attemptTrack);

        double coverageRatio = coverageCalculator.computeCoverage(
                attemptTrack,
                route,
                COVERAGE_TOLERANCE_M
        );

        double maxDeviationM = TrackMetrics.computeMaxDeviationMeters(
                attemptTrack,
                route
        );

        // ---------------------------------------
        // Step 4 — Difficulty scoring via polymorphic model
        // ---------------------------------------
        double difficultyScore = difficultyModel.computeScore(
                distanceKm,
                elevationGainM,
                coverageRatio,
                maxDeviationM
        );

        // ---------------------------------------
        // Step 5 — Result classification
        // ---------------------------------------
        String result;
        if (coverageRatio < 0.50 || Double.isNaN(maxDeviationM)) {
            result = "REJECTED";
        } else if (coverageRatio < 0.90) {
            result = "FLAGGED";
        } else {
            result = "VERIFIED";
        }

        // ---------------------------------------
        // Step 6 — Build and persist Attempt (with GPX blob)
        // ---------------------------------------
        Attempt attempt = new Attempt();
        attempt.setRunnerId(runnerId);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setDistanceKm(distanceKm);
        attempt.setElevationGainM(elevationGainM);
        attempt.setDifficultyScore(difficultyScore);
        attempt.setCoverageRatio(coverageRatio);
        attempt.setMaxDeviationM(maxDeviationM);
        attempt.setResult(result);
        attempt.setMessage("Verification completed using OOP strategy classes.");
        attempt.setGpxData(gpxFile.getBytes());   // IMPORTANT: persist GPX blob

        return attemptRepository.save(attempt);
    }

    /**
     * Standardized rejection builder.
     */
    private Attempt buildRejectedAttempt(String runnerId, String message) {
        Attempt attempt = new Attempt();
        attempt.setRunnerId(runnerId);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setDistanceKm(0.0);
        attempt.setElevationGainM(0.0);
        attempt.setDifficultyScore(0.0);
        attempt.setCoverageRatio(0.0);
        attempt.setMaxDeviationM(null);
        attempt.setResult("REJECTED");
        attempt.setMessage(message);
        attempt.setGpxData(null);
        return attemptRepository.save(attempt);
    }
}
