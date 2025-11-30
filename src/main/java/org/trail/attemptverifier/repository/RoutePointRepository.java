package org.trail.attemptverifier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trail.attemptverifier.model.RoutePoint;

public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {
}
