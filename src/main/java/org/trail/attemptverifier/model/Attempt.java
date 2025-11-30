package org.trail.attemptverifier.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "runner_id", nullable = false)
    private String runnerId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime attemptTime;

    @Column(name = "distance_km")
    private double distanceKm;

    @Column(name = "elevation_gain_m")
    private double elevationGainM;

    @Column(name = "difficulty_score")
    private double difficultyScore;

    @Column(name = "result")
    private String result;

    @Column(name = "message", length = 500)
    private String message;

    // --- Persisted calculated metrics ---
    @Column(name = "coverage_ratio")
    private Double coverageRatio;

    @Column(name = "max_deviation_m")
    private Double maxDeviationM;

    // --- Non-persisted fields used only for API response ---
    @Transient
    private boolean isOfficialRouteUsed;

    @Transient
    private String debugInfo;

    public Attempt() {}

    // Convenience constructor for service layer
    public Attempt(String runnerId) {
        this.runnerId = runnerId;
        this.attemptTime = LocalDateTime.now();
    }

    // ---------------- Getters & Setters ----------------

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getRunnerId() { return runnerId; }

    public void setRunnerId(String runnerId) { this.runnerId = runnerId; }

    public LocalDateTime getAttemptTime() { return attemptTime; }

    public void setAttemptTime(LocalDateTime attemptTime) { this.attemptTime = attemptTime; }

    public double getDistanceKm() { return distanceKm; }

    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getElevationGainM() { return elevationGainM; }

    public void setElevationGainM(double elevationGainM) { this.elevationGainM = elevationGainM; }

    public double getDifficultyScore() { return difficultyScore; }

    public void setDifficultyScore(double difficultyScore) { this.difficultyScore = difficultyScore; }

    public String getResult() { return result; }

    public void setResult(String result) { this.result = result; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public Double getCoverageRatio() { return coverageRatio; }

    public void setCoverageRatio(Double coverageRatio) { this.coverageRatio = coverageRatio; }

    public Double getMaxDeviationM() { return maxDeviationM; }

    public void setMaxDeviationM(Double maxDeviationM) { this.maxDeviationM = maxDeviationM; }

    public boolean isOfficialRouteUsed() { return isOfficialRouteUsed; }

    public void setOfficialRouteUsed(boolean officialRouteUsed) { isOfficialRouteUsed = officialRouteUsed; }

    public String getDebugInfo() { return debugInfo; }

    public void setDebugInfo(String debugInfo) { this.debugInfo = debugInfo; }
}
