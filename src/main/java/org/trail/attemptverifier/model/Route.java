package org.trail.attemptverifier.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private double totalDistanceKm;

    @Column(nullable = false)
    private double totalElevationM;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoutePoint> points;

    public Route() {}

    public Route(String routeName, double totalDistanceKm, double totalElevationM) {
        this.routeName = routeName;
        this.totalDistanceKm = totalDistanceKm;
        this.totalElevationM = totalElevationM;
    }

    public Long getId() {
        return id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public double getTotalElevationM() {
        return totalElevationM;
    }

    public void setTotalElevationM(double totalElevationM) {
        this.totalElevationM = totalElevationM;
    }

    public List<RoutePoint> getPoints() {
        return points;
    }

    public void setPoints(List<RoutePoint> points) {
        this.points = points;
    }
}
