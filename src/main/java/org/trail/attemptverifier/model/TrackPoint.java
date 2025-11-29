package org.trail.attemptverifier.model;

import java.time.Instant;

public class TrackPoint {

    private double latitude;
    private double longitude;
    private Double elevation; // may be null
    private Instant time;     // may be null

    public TrackPoint() {
    }

    public TrackPoint(double latitude, double longitude, Double elevation, Instant time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
