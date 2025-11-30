package org.trail.attemptverifier.model;

import java.time.LocalDateTime;

/**
 * Plain Attempt domain model (no JPA).
 * Persistence is handled manually via AttemptRepository (JdbcTemplate).
 */
public class Attempt {

    private Long id;
    private String runnerId;
    private LocalDateTime attemptTime;

    private double distanceKm;
    private double elevationGainM;
    private double difficultyScore;

    private String result;
    private String message;

    // Persisted calculated metrics
    private Double coverageRatio;
    private Double maxDeviationM;

    // Optional: raw GPX bytes for visualization / debugging
    private byte[] gpxData;

    // Non-persisted helper flags (used only in API / debugging)
    private boolean officialRouteUsed;
    private String debugInfo;

    public Attempt() {
    }

    // Convenience constructor for service layer
    public Attempt(String runnerId) {
        this.runnerId = runnerId;
        this.attemptTime = LocalDateTime.now();
    }

    // ---------------- Getters & Setters ----------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }

    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    public void setAttemptTime(LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getElevationGainM() {
        return elevationGainM;
    }

    public void setElevationGainM(double elevationGainM) {
        this.elevationGainM = elevationGainM;
    }

    public double getDifficultyScore() {
        return difficultyScore;
    }

    public void setDifficultyScore(double difficultyScore) {
        this.difficultyScore = difficultyScore;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getCoverageRatio() {
        return coverageRatio;
    }

    public void setCoverageRatio(Double coverageRatio) {
        this.coverageRatio = coverageRatio;
    }

    public Double getMaxDeviationM() {
        return maxDeviationM;
    }

    public void setMaxDeviationM(Double maxDeviationM) {
        this.maxDeviationM = maxDeviationM;
    }

    public byte[] getGpxData() {
        return gpxData;
    }

    public void setGpxData(byte[] gpxData) {
        this.gpxData = gpxData;
    }

    public boolean isOfficialRouteUsed() {
        return officialRouteUsed;
    }

    public void setOfficialRouteUsed(boolean officialRouteUsed) {
        this.officialRouteUsed = officialRouteUsed;
    }

    public String getDebugInfo() {
        return debugInfo;
    }

    public void setDebugInfo(String debugInfo) {
        this.debugInfo = debugInfo;
    }
}
