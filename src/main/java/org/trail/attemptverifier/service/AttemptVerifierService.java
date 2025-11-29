package org.trail.attemptverifier.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.trail.attemptverifier.model.Attempt;
import org.trail.attemptverifier.model.TrackPoint;
import org.trail.attemptverifier.repository.AttemptRepository;
import org.trail.attemptverifier.util.GpxParser;
import org.trail.attemptverifier.util.TrackMetrics;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttemptVerifierService {

    private final AttemptRepository attemptRepository;
    private final GpxParser gpxParser;
    private final RouteService routeService;

    public AttemptVerifierService(AttemptRepository attemptRepository,
                                  GpxParser gpxParser,
                                  RouteService routeService) {
        this.attemptRepository = attemptRepository;
        this.gpxParser = gpxParser;
        this.routeService = routeService;
    }

    public Attempt verifyAttempt(MultipartFile gpxFile, String runnerId) {
        double distanceKm = 0.0;
        double elevationGainM = 0.0;
        double difficultyScore = 0.0;
        double coverageRatio = 0.0;
        Double maxDeviationM = null;
        String result;
        String message;

        try {
            // 1. Parse attempt GPX
            List<TrackPoint> attemptTrack = gpxParser.parse(gpxFile.getInputStream());

            if (attemptTrack == null || attemptTrack.isEmpty()) {
                result = "REJECTED";
                message = "No valid track points could be parsed from the GPX file.";
            } else {
                // 2. Basic metrics from attempt
                distanceKm = TrackMetrics.computeTotalDistanceKm(attemptTrack);
                elevationGainM = TrackMetrics.computeElevationGainM(attemptTrack);

                // 3. Route metrics
                List<TrackPoint> routeTrack = routeService.getTrackPoints();
                double routeDistanceKm = routeService.getRouteDistanceKm();

                if (!routeTrack.isEmpty() && routeDistanceKm > 0.0) {
                    // coverage within 100 m
                    coverageRatio = TrackMetrics.computeCoverageRatio(
                            attemptTrack, routeTrack, 100.0
                    );
                    maxDeviationM = TrackMetrics.computeMaxDeviationMeters(
                            attemptTrack, routeTrack
                    );

                    double distanceRatio = distanceKm / routeDistanceKm;

                    // 4. Difficulty (simple for now)
                    difficultyScore = distanceKm + elevationGainM / 100.0;

                    // 5. Classification rules
                    if (distanceRatio >= 0.97 && distanceRatio <= 1.03 &&
                            coverageRatio >= 0.90 &&
                            maxDeviationM != null && maxDeviationM <= 50.0) {
                        result = "VERIFIED";
                        message = "Attempt closely matches the official route (distance and coverage).";
                    } else if (distanceRatio >= 0.80 && distanceRatio <= 1.20 &&
                            coverageRatio >= 0.60 &&
                            maxDeviationM != null && maxDeviationM <= 150.0) {
                        result = "FLAGGED";
                        message = "Attempt roughly follows the official route but has noticeable deviations.";
                    } else {
                        result = "REJECTED";
                        message = "Attempt significantly deviates from the official route in distance or position.";
                    }
                } else {
                    // No official route available – fall back to simple rules
                    difficultyScore = distanceKm + elevationGainM / 100.0;

                    if (distanceKm > 0.5) {
                        result = "FLAGGED";
                        message = "Attempt processed without official route comparison.";
                    } else {
                        result = "REJECTED";
                        message = "Track too short and no official route available.";
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[AttemptVerifierService] ERROR processing GPX: " + e.getMessage());
            result = "REJECTED";
            message = "Failed to process GPX file: " + e.getClass().getSimpleName();
        }

        Attempt attempt = new Attempt();
        attempt.setRunnerId(runnerId);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setDistanceKm(distanceKm);
        attempt.setElevationGainM(elevationGainM);
        attempt.setDifficultyScore(difficultyScore);
        attempt.setResult(result);
        attempt.setMessage(message);
        attempt.setCoverageRatio(coverageRatio);
        attempt.setMaxDeviationM(maxDeviationM);

        return attemptRepository.save(attempt);
    }
}
