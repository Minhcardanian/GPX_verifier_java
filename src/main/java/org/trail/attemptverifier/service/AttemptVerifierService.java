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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core business logic for verifying runner GPX attempts.
 * Demonstrates:
 *  - Encapsulation (service owns strategy components)
 *  - Polymorphism (difficulty/coverage via interfaces)
 *  - MVC layering and separation of concerns
 */
@Service
public class AttemptVerifierService {

    private final AttemptRepository attemptRepository;
    private final GpxParser gpxParser;
    private final RouteService routeService;

    // OOP strategy instances (polymorphism)
    private final DifficultyModel difficultyModel = new DefaultDifficultyModel();
    private final CoverageCalculator coverageCalculator = new DefaultCoverageCalculator();

    // Route coverage tolerance threshold
    private static final double COVERAGE_TOLERANCE_M = 30.0;

    public AttemptVerifierService(AttemptRepository attemptRepository,
                                  GpxParser gpxParser,
                                  RouteService routeService) {
        this.attemptRepository = attemptRepository;
        this.gpxParser = gpxParser;
        this.routeService = routeService;
    }

    /**
     * Main verification pipeline:
     * 1. Read GPX bytes
     * 2. Parse → TrackPoint list
     * 3. Load official route
     * 4. Compute metrics (distance, elevation, coverage, deviation)
     * 5. Score difficulty (strategy)
     * 6. Classify (VERIFIED / FLAGGED / REJECTED)
     * 7. Persist Attempt (including raw GPX bytes) into DB
     */
    public Attempt verifyAttempt(MultipartFile gpxFile, String runnerId) throws IOException {

        // ---------------------------------------
        // Step 1 — Read GPX bytes
        // ---------------------------------------
        byte[] rawBytes;
        try {
            rawBytes = gpxFile.getBytes();
        } catch (IOException e) {
            System.err.println("[AttemptVerifierService] Failed to read GPX bytes: " + e.getMessage());
            return buildRejectedAttempt(runnerId, "Could not read GPX file.");
        }

        // ---------------------------------------
        // Step 2 — Parse GPX
        // ---------------------------------------
        List<TrackPoint> attemptTrack;
        try (InputStream in = new ByteArrayInputStream(rawBytes)) {
            attemptTrack = gpxParser.parse(in);
        } catch (Exception e) {
            System.err.println("[AttemptVerifierService] GPX parse error: " + e.getMessage());
            return buildRejectedAttempt(runnerId, "Invalid GPX content.");
        }

        if (attemptTrack.isEmpty()) {
            return buildRejectedAttempt(runnerId, "No valid track points found.");
        }

        // ---------------------------------------
        // Step 3 — Load official route
        // ---------------------------------------
        List<TrackPoint> route = routeService.getTrackPoints();
        if (route == null || route.isEmpty()) {
            return buildRejectedAttempt(runnerId, "Official route not available.");
        }

        // ---------------------------------------
        // Step 4 — Compute metrics
        // ---------------------------------------
        double distanceKm = TrackMetrics.computeTotalDistanceKm(attemptTrack);
        double elevationGainM = TrackMetrics.computeElevationGainM(attemptTrack);

        double coverageRatio = coverageCalculator.computeCoverage(
                attemptTrack,
                route,
                COVERAGE_TOLERANCE_M
        );

        double maxDeviationM = TrackMetrics.computeMaxDeviationMeters(attemptTrack, route);

        // ---------------------------------------
        // Step 5 — OOP difficulty score
        // ---------------------------------------
        double difficultyScore = difficultyModel.computeScore(
                distanceKm,
                elevationGainM,
                coverageRatio,
                maxDeviationM
        );

        // ---------------------------------------
        // Step 6 — Classification
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
        // Step 7 — Build and persist Attempt
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
        attempt.setGpxData(rawBytes);   // NEW: store GPX bytes

        return attemptRepository.save(attempt);
    }

    /**
     * Helper for standardizing rejected attempts.
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

    /**
     * Load and parse the stored GPX for a given attempt.
     * Used by /api/attempts/{id}/track for the map.
     */
    public List<TrackPoint> loadAttemptTrack(Long attemptId) {
        return attemptRepository.findById(attemptId)
                .map(attempt -> {
                    byte[] data = attempt.getGpxData();
                    if (data == null || data.length == 0) {
                        return List.<TrackPoint>of();
                    }
                    try (InputStream in = new ByteArrayInputStream(data)) {
                        return gpxParser.parse(in);
                    } catch (IOException e) {
                        System.err.println("[AttemptVerifierService] Failed to re-parse GPX from DB: "
                                + e.getMessage());
                        return List.<TrackPoint>of();
                    }
                })
                .orElse(List.of());
    }
}
