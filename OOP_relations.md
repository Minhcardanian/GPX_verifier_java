# OOP Relationships Overview

This document summarizes object-oriented relationships across the codebase, including inheritance/implementation, composition, and notable overridden methods.

## Interfaces and Implementations

- `org.trail.attemptverifier.service.oop.DifficultyModel` (interface)
  - Contract: `computeScore(distanceKm, elevationGainM, coverageRatio, maxDeviationM)`.
  - Implemented by `DefaultDifficultyModel`, which overrides `computeScore` to combine distance, elevation, coverage bonus, and deviation penalty into a non-negative score.【F:src/main/java/org/trail/attemptverifier/service/oop/DifficultyModel.java†L5-L23】【F:src/main/java/org/trail/attemptverifier/service/oop/DefaultDifficultyModel.java†L5-L34】

- `org.trail.attemptverifier.service.oop.CoverageCalculator` (interface)
  - Contract: `computeCoverage(attemptTrack, routeTrack, toleranceM)` returning a 0–1 coverage ratio.
  - Implemented by `DefaultCoverageCalculator`, which overrides `computeCoverage` with a downsampling strategy and sliding nearest-neighbour search; helper methods `downsample`, `distanceMeters`, and `haversineMeters` are encapsulated within the class.【F:src/main/java/org/trail/attemptverifier/service/oop/CoverageCalculator.java†L5-L24】【F:src/main/java/org/trail/attemptverifier/service/oop/DefaultCoverageCalculator.java†L5-L88】

- `org.springframework.jdbc.core.RowMapper<Attempt>` (interface from Spring)
  - Implemented by the private static inner class `AttemptRepository.AttemptRowMapper`, which overrides `mapRow` to hydrate `Attempt` domain objects from JDBC result sets, including optional Double and BLOB fields.【F:src/main/java/org/trail/attemptverifier/repository/AttemptRepository.java†L20-L54】

## Service Layer Composition

- `AttemptVerifierService`
  - Composes repository (`AttemptRepository`), utility parser (`GpxParser`), and route loader (`RouteService`).【F:src/main/java/org/trail/attemptverifier/service/AttemptVerifierService.java†L28-L43】
  - Encapsulates strategy objects through interface references (`DifficultyModel difficultyModel = new DefaultDifficultyModel()`, `CoverageCalculator coverageCalculator = new DefaultCoverageCalculator()`), demonstrating polymorphism and the ability to swap implementations.【F:src/main/java/org/trail/attemptverifier/service/AttemptVerifierService.java†L31-L38】
  - Coordinates the verification workflow (`verifyAttempt`), delegating metrics to `TrackMetrics` static helpers and strategy interfaces; constructs and persists `Attempt` instances, illustrating composition over inheritance.【F:src/main/java/org/trail/attemptverifier/service/AttemptVerifierService.java†L47-L142】
  - Uses `buildRejectedAttempt` as a factory-style helper to standardize failure cases and persist them via the repository.【F:src/main/java/org/trail/attemptverifier/service/AttemptVerifierService.java†L144-L173】

- `RouteService`
  - Composes `GpxParser` and `ResourceLoader` to lazily load and cache the official route GPX; exposes `getTrackPoints` for downstream services.【F:src/main/java/org/trail/attemptverifier/service/RouteService.java†L5-L45】

## Controller Layer Dependencies

- `AttemptController`
  - Depends on `AttemptVerifierService` for business logic and `AttemptRepository` for direct data queries; endpoints return domain models or DTOs. Nested DTO classes (`ErrorResponse`, `ResetResponse`) encapsulate simple data without inheritance.【F:src/main/java/org/trail/attemptverifier/controller/AttemptController.java†L17-L45】【F:src/main/java/org/trail/attemptverifier/controller/AttemptController.java†L124-L190】

- `HealthController`
  - Simple REST controller with a single endpoint returning a health string; no inheritance beyond Spring’s annotations.【F:src/main/java/org/trail/attemptverifier/controller/HealthController.java†L1-L13】

## Domain and Utility Classes

- `Attempt` and `TrackPoint`
  - Plain POJOs with state and accessor methods; they do not extend or implement custom interfaces but serve as domain entities composed within services, controllers, and repositories.【F:src/main/java/org/trail/attemptverifier/model/Attempt.java†L5-L118】【F:src/main/java/org/trail/attemptverifier/model/TrackPoint.java†L5-L40】

- `TrackMetrics`
  - Utility class combining instance fields with static factory/helper methods. Although not implementing an interface, it demonstrates encapsulation of metric calculations (distance, elevation gain, coverage ratio, max deviation) used by services.【F:src/main/java/org/trail/attemptverifier/util/TrackMetrics.java†L5-L118】

- `GpxParser`
  - Spring component encapsulating DOM-based GPX parsing logic to produce `TrackPoint` objects, isolating parsing concerns from services.【F:src/main/java/org/trail/attemptverifier/util/GpxParser.java†L5-L74】

## Application Entry Point

- `AttemptVerifierApplication`
  - Annotated with `@SpringBootApplication`; provides the static `main` entry point to bootstrap the Spring context. No inheritance or overrides beyond `SpringApplication.run` call.【F:src/main/java/org/trail/attemptverifier/AttemptVerifierApplication.java†L1-L11】
