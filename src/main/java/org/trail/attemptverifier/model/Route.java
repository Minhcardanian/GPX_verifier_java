package org.trail.attemptverifier.model;

import java.util.List;

public class Route {

    private String name;
    private List<TrackPoint> trackPoints;

    public Route(String name, List<TrackPoint> trackPoints) {
        this.name = name;
        this.trackPoints = trackPoints;
    }

    public String getName() {
        return name;
    }

    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }
}
