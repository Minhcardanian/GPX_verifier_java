package org.trail.attemptverifier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trail.attemptverifier.model.Runner;

import java.util.Optional;

public interface RunnerRepository extends JpaRepository<Runner, Long> {
    Optional<Runner> findByRunnerId(String runnerId);
}
