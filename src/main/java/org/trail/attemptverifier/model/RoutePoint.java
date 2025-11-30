package org.trail.attemptverifier.model;

import jakarta.persistence.*;

@Entity
@Table(name = "route_points")
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    @Column(nullable = false)
    private double elevationM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    public RoutePoint() {}

    public RoutePoint(double lat, double lon, double elevationM, Route route) {
        this.lat = lat;
        this.lon = lon;
        this.elevationM = elevationM;
        this.route = route;
    }

    public Long getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getElevationM() {
        return elevationM;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
