package org.trail.attemptverifier.model;

import java.time.LocalDateTime;

public class Attempt {

    private Long id;
    private String runnerId;
    private LocalDateTime attemptTime;
    private double distanceKm;
    private double elevationGainM;
    private double difficultyScore;
    private String result;
    private String message;

    // Derived / non-persisted fields - used only in API responses
    private Double coverageRatio;   // 0.0–1.0
    private Double maxDeviationM;   // meters

    public Attempt() {
    }

    // ---- Getters & Setters ----

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
}
