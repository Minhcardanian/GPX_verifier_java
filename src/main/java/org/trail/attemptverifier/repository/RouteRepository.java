package org.trail.attemptverifier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trail.attemptverifier.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
