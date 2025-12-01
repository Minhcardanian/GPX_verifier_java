 GPX Attempt Verifier – Technical README

## 1. Project Overview
GPX Attempt Verifier is a Spring Boot 3.3.4 application that authenticates trail-running GPX uploads against an official race route. It parses runner-submitted GPX tracks, computes distance, elevation, coverage, and deviation metrics, scores attempt difficulty, and classifies each upload as VERIFIED, FLAGGED, or REJECTED. Results and raw GPX bytes are persisted to a relational database (MySQL by default) for later inspection and visualization.

## 2. Architecture Overview
The system follows a layered architecture:
- **Web/API layer**: Spring MVC REST controllers expose endpoints for uploading GPX files and querying attempts (`AttemptController`, `HealthController`).
- **Service layer**: Business logic orchestrated by `AttemptVerifierService` and supporting `RouteService` for official route loading.
- **Persistence layer**: `AttemptRepository` uses `JdbcTemplate` for CRUD operations against the `attempts` table.
- **Utility layer**: `GpxParser` (DOM-based GPX parsing) and `TrackMetrics` (distance, elevation, coverage, deviation calculations) plus strategy interfaces in `service.oop`.

Architecture flow (textual diagram): Browser/HTTP client → REST controllers → service layer → utilities (parsing/metrics) → repository → database. The service layer also calls `RouteService` to load cached official route data. Technology stack: Java 17, Spring Boot 3.3.4, Spring MVC, Spring JDBC, MySQL (schema script provided) or compatible JDBC database, Maven build.

## 3. Class Analysis
### 3.1 Objects
- **Attempt** (`org.trail.attemptverifier.model.Attempt`): State: `id`, `runnerId`, `attemptTime`, `distanceKm`, `elevationGainM`, `difficultyScore`, `result`, `message`, `coverageRatio`, `maxDeviationM`, `gpxData`, `officialRouteUsed`, `debugInfo` with getters/setters. Behavior: constructors for default and runnerId initialization. Responsibility: represents a verification record and associated metrics stored in DB.
- **TrackPoint** (`org.trail.attemptverifier.model.TrackPoint`): State: `latitude`, `longitude`, optional `elevation`, optional `time`; behavior: constructors and accessors. Responsibility: value object for GPX track points.
- **AttemptVerifierService**: Coordinates verification pipeline, owns strategy instances `DifficultyModel`/`CoverageCalculator`, performs parsing, metric computation, classification, and persistence. Responsibility: core business logic.
- **RouteService**: Loads and caches the official route GPX from classpath using `GpxParser`. Responsibility: provide reference track for comparisons.
- **AttemptRepository**: JDBC repository encapsulating CRUD and filter queries plus reset operation. Responsibility: map DB rows to `Attempt` and persist new attempts.
- **GpxParser**: DOM-based parser producing `TrackPoint` list from GPX XML. Responsibility: robust parsing with attribute and child-element handling.
- **TrackMetrics**: Static helpers and data container for computing distance, elevation gain, coverage ratio, and maximum deviation via haversine calculations and downsampling. Responsibility: computational utilities for tracks.
- **DefaultDifficultyModel / DifficultyModel**: Interface and default heuristic scoring implementation. Responsibility: plug-in difficulty scoring strategy.
- **DefaultCoverageCalculator / CoverageCalculator**: Interface and optimized coverage computation implementation (downsampling + sliding nearest neighbor). Responsibility: compute route coverage ratio.
- **AttemptController / HealthController**: REST controllers exposing verification, query, GPX retrieval, track retrieval, and reset endpoints; health check endpoint. Responsibility: HTTP interface.

### 3.2 Object Grouping
- **Domain entities**: `Attempt`, `TrackPoint` encapsulate core domain state persisted or transferred across layers.
- **Service objects**: `AttemptVerifierService`, `RouteService` orchestrate business processes and route loading.
- **Repositories**: `AttemptRepository` isolates persistence concerns from services.
- **Strategy helpers (OOP)**: Interfaces and default implementations in `service.oop` enable interchangeable coverage/difficulty algorithms.
- **Utilities**: `GpxParser`, `TrackMetrics` provide reusable parsing and metric computations decoupled from services.
- **Presentation components**: Static dashboard (`src/main/resources/static/index.html`) consuming REST endpoints for upload, listing, mapping, and charts.

## 4. Class Design
### 4.1 Class Diagram (Narrative)
Controllers depend on services (`AttemptController` → `AttemptVerifierService`, `AttemptRepository`; health standalone). `AttemptVerifierService` aggregates `GpxParser`, `RouteService`, and strategy interfaces (`DifficultyModel`, `CoverageCalculator`) and delegates persistence to `AttemptRepository`. `RouteService` uses `GpxParser` and Spring `ResourceLoader`. `AttemptRepository` maps `Attempt` to relational schema using `JdbcTemplate`. Utilities (`GpxParser`, `TrackMetrics`) are stateless; strategy interfaces have default concrete implementations. Associations: `AttemptVerifierService` uses composition for strategy objects; `AttemptRepository` aggregates `AttemptRowMapper` for entity mapping. No inheritance hierarchies beyond interface implementation.

### 4.2 Detailed Class Table
| Class Name | Package | Key Attributes | Key Methods | Responsibility |
| --- | --- | --- | --- | --- |
| Attempt | org.trail.attemptverifier.model | id, runnerId, attemptTime, distanceKm, elevationGainM, difficultyScore, result, message, coverageRatio, maxDeviationM, gpxData | getters/setters, constructors | Domain record of a verification attempt. |
| TrackPoint | org.trail.attemptverifier.model | latitude, longitude, elevation, time | constructors, getters/setters | GPX point representation. |
| AttemptVerifierService | org.trail.attemptverifier.service | AttemptRepository, GpxParser, RouteService, DifficultyModel, CoverageCalculator | verifyAttempt, loadAttemptTrack | Core verification pipeline and DB persistence. |
| RouteService | org.trail.attemptverifier.service | GpxParser, ResourceLoader, cachedRoute | getTrackPoints | Load/caches official route points. |
| AttemptRepository | org.trail.attemptverifier.repository | JdbcTemplate | save, findAll, findById, filters, resetAll | JDBC CRUD for attempts table. |
| GpxParser | org.trail.attemptverifier.util | — | parse | DOM GPX parser returning TrackPoint list. |
| TrackMetrics | org.trail.attemptverifier.util | distanceKm, elevationGainM, coverageRatio, maxDeviationM | computeTotalDistanceKm, computeElevationGainM, computeCoverageRatio, computeMaxDeviationMeters | Metric computations for tracks. |
| DifficultyModel (interface) | org.trail.attemptverifier.service.oop | — | computeScore | Contract for difficulty scoring. |
| DefaultDifficultyModel | org.trail.attemptverifier.service.oop | — | computeScore | Heuristic difficulty implementation. |
| CoverageCalculator (interface) | org.trail.attemptverifier.service.oop | — | computeCoverage | Contract for route coverage computation. |
| DefaultCoverageCalculator | org.trail.attemptverifier.service.oop | MAX_POINTS | computeCoverage, helpers | Optimized coverage calculation with downsampling/haversine. |
| AttemptController | org.trail.attemptverifier.controller | AttemptVerifierService, AttemptRepository | uploadAttempt, listAttempts, getAttempt, getAttemptTrack, getAttemptGpx, resetAttempts | REST API façade. |
| HealthController | org.trail.attemptverifier.controller | — | health | Health probe endpoint. |

### 4.3 Abstract Classes
No abstract classes are defined. Instead, interfaces (`DifficultyModel`, `CoverageCalculator`) capture behavioral contracts for polymorphic strategies. Interfaces are appropriate to allow alternative implementations without inheritance constraints.

## 5. OOP Techniques
### 5.1 Encapsulation
Domain fields are private with public getters/setters (e.g., `Attempt` uses private fields such as `distanceKm` with controlled access). Services encapsulate processing steps internally (`verifyAttempt` hides parsing and classification details).

### 5.2 Inheritance
Inheritance is via interface implementation: `DefaultDifficultyModel` implements `DifficultyModel`, and `DefaultCoverageCalculator` implements `CoverageCalculator`, enabling substitution of strategies in the service layer.

### 5.3 Polymorphism
`AttemptVerifierService` declares fields of interface types and instantiates default implementations, enabling runtime polymorphism for coverage and difficulty strategies. Overriding occurs in concrete classes implementing the interfaces (custom scoring/coverage could replace defaults). Framework-driven polymorphism appears in controllers through Spring MVC method dispatch.

### 5.4 Interfaces
`DifficultyModel` and `CoverageCalculator` define contracts for scoring and coverage; `DefaultDifficultyModel` and `DefaultCoverageCalculator` fulfill them. `AttemptVerifierService` depends on the interfaces rather than concrete classes, allowing future alternative strategies (e.g., machine-learning-based scoring).

## 6. Access Control Analysis
### 6.1 Data Access Table
| Data/Field | Class | Modifier | Justification |
| --- | --- | --- | --- |
| id, runnerId, distanceKm, etc. | Attempt | private | Enforces encapsulation; accessed via getters/setters for validation and persistence mapping. |
| latitude, longitude, elevation, time | TrackPoint | private | Protects invariants; accessed via getters/setters for parsing and metrics. |
| distanceKm, elevationGainM, coverageRatio, maxDeviationM | TrackMetrics | private | Keeps computed metrics immutable unless updated through setters or constructors. |
| difficultyModel, coverageCalculator | AttemptVerifierService | private final | Strategy instances hidden from callers, ensuring controlled usage. |

### 6.2 Method Access Table
| Method | Class | Modifier | Rationale |
| --- | --- | --- | --- |
| verifyAttempt, loadAttemptTrack | AttemptVerifierService | public | Exposed to controllers for business operations. |
| save, findAll, resetAll | AttemptRepository | public | CRUD API for services/controllers. |
| parse | GpxParser | public | Utility entry point for GPX parsing used across services. |
| computeScore (override) | DefaultDifficultyModel | public | Implements interface contract for scoring. |

## 7. Package Design
Package hierarchy:
```
org.trail.attemptverifier
├─ controller
├─ service
│  └─ oop
├─ repository
├─ model
└─ util
```
- `controller`: HTTP entry points, returning JSON/GPX responses.
- `service`: Business orchestration and route loading; `service.oop` holds strategy interfaces/implementations for extensibility.
- `repository`: Database access via JDBC templates.
- `model`: Domain objects shared across layers.
- `util`: Parsing and metric calculations to keep services cohesive and reusable.
The separation increases cohesion within each package and lowers coupling by isolating responsibilities.

## 8. Database Design
### 8.1 Entities and ERD (Textual)
Single table `attempts` with primary key `id` (BIGINT AUTO_INCREMENT). Columns: `runner_id` (VARCHAR), `timestamp` (DATETIME), `distance_km` (DOUBLE), `elevation_gain_m` (DOUBLE), `difficulty_score` (DOUBLE), `result` (VARCHAR), `message` (VARCHAR). Relationships: currently none to other tables; each row represents one verification attempt. Equivalent ERD: entity Attempt(id PK, runnerId, timestamp, distanceKm, elevationGainM, difficultyScore, result, message). Additional metrics (`coverage_ratio`, `max_deviation_m`, `gpx_data`) are mapped in repository though not explicitly defined in script (schema can be extended accordingly).

### 8.2 Data Flow
1. User uploads GPX via `/api/attempts/upload` (multipart). Controller forwards to `AttemptVerifierService.verifyAttempt`.
2. Service reads bytes, parses GPX into `TrackPoint` list using `GpxParser`, loads official route via `RouteService`, computes metrics (`TrackMetrics` and strategy calculators), classifies result, and constructs `Attempt` with raw GPX bytes.
3. `AttemptRepository.save` inserts a row into `attempts`, storing metrics and blob data.
4. Subsequent queries retrieve attempts or GPX for visualization through repository methods invoked by controllers.

## 9. Algorithms & Business Logic
### 9.1 GPX Parsing
`GpxParser` uses DOM (`DocumentBuilderFactory` with namespace awareness) to locate `trkpt` nodes, extract `lat`/`lon` attributes, optional `ele` and `time` children, and construct `TrackPoint` objects. Malformed points are skipped; parse errors are logged, and the caller handles empty results.

### 9.2 Route Matching Algorithm
Coverage and deviation computations use downsampling and haversine distance:
1. Downsample attempt and route to maximum 5000 points to control complexity (`DefaultCoverageCalculator`, `TrackMetrics`).
2. For each attempt point, advance along the route while the next point is closer (sliding nearest-neighbor).
3. Coverage ratio = covered points within tolerance (30 m default) / total points.
4. Maximum deviation uses similar sliding search to find the farthest distance from route across points.

### 9.3 Difficulty Scoring
`DefaultDifficultyModel` computes score = distanceKm + elevationGainM/100 + coverageBonus (coverageRatio * 10) − deviationPenalty (bounded, or fixed if deviation unknown), floored at 0. Inputs: distance, elevation gain, coverage ratio, max deviation.

### 9.4 Classification Logic
`AttemptVerifierService` classifies:
- REJECTED if coverageRatio < 0.50 or deviation not computable (NaN).
- FLAGGED if coverageRatio between 0.50 and < 0.90.
- VERIFIED otherwise (coverage ≥ 0.90).
Message “Verification completed using OOP strategy classes.” stored with result.

## 10. REST API Design
### 10.1 Endpoint Summary Table
| HTTP Method | Endpoint Path | Request Body | Response | Description |
| --- | --- | --- | --- | --- |
| GET | `/api/health` | None | String OK message | Health probe. |
| POST | `/api/attempts/upload` | multipart: runnerId, file (GPX) | Attempt JSON or error DTO | Uploads GPX, verifies, persists result. |
| GET | `/api/attempts` | Query params runner, result (optional) | List<Attempt> | List attempts with optional filtering. |
| GET | `/api/attempts/{id}` | Path variable id | Attempt or error DTO | Fetch single attempt metadata. |
| GET | `/api/attempts/{id}/track` | Path id | List<TrackPoint> | Parsed GPX points for mapping. |
| GET | `/api/attempts/{id}/gpx` | Path id | GPX XML bytes | Raw GPX file from DB. |
| DELETE | `/api/attempts/reset` | None | ResetResponse | Delete all attempts and reset auto-increment. |

### 10.2 Example Request/Response
**Request (multipart upload):**
```
POST /api/attempts/upload
Content-Type: multipart/form-data
- runnerId=runner123
- file=@sample.gpx
```
**Response (success):**
```
{
  "id": 7,
  "runnerId": "runner123",
  "distanceKm": 42.3,
  "elevationGainM": 2100.5,
  "difficultyScore": 62.1,
  "coverageRatio": 0.93,
  "maxDeviationM": 18.4,
  "result": "VERIFIED",
  "message": "Verification completed using OOP strategy classes."
}
```

## 11. Environment and Tools
- **Java**: 17 (configured in Maven).
- **Spring Boot**: 3.3.4 (parent BOM).
- **Database**: MySQL via JDBC; script `db/attempt_verifier_db.sql` creates schema and user. SQLite-compatible with JDBC could be configured by adjusting properties.
- **Build tool**: Maven (`spring-boot-maven-plugin`).
- **Libraries**: Spring Web, Spring JDBC, MySQL Connector/J, optional validation and Lombok, Spring Boot Test.
- **Run/build**: `mvn spring-boot:run` to start; `mvn package` to build jar. Configure DB via `src/main/resources/application.properties` (JDBC URL, credentials, upload limits).

## 12. Project Functions
- **Upload GPX and verify**: Input multipart GPX + runnerId; process through parsing, metrics, classification; output Attempt JSON saved to DB.
- **View attempt verification details**: Input optional filters; service retrieves attempts; output list or single attempt JSON for UI listing and details.
- **Retrieve GPX/track for visualization**: Input attempt id; service reloads stored GPX, parses to TrackPoint list or returns raw GPX bytes; output for map overlay and elevation chart.
- **Reset database**: Delete all attempts and reset auto-increment via DELETE endpoint, supporting clean demos/testing.

## 13. GUI / Front-end
A static dashboard (`index.html`) consumes REST endpoints. Key components: upload form (runnerId + GPX), attempt table with filters, Leaflet map modal to draw runner path and official route (fetches `/api/attempts/{id}/track` and `/api/route/track` if available), and Chart.js elevation plot. Front-end uses fetch calls to backend endpoints for data loading and upload handling.

## 14. Duty Roster
| ID | Task | In Charge | Start | End | State |
| --- | --- | --- | --- | --- | --- |
| 061CSE215-A | Backend REST API, JDBC repository, service algorithms | Developer A | 2024-10-01 | 2024-10-15 | Completed |
| 061CSE215-B | GPX parsing utilities and metric computations | Developer B | 2024-10-05 | 2024-10-18 | Completed |
| 061CSE215-C | Front-end dashboard (Leaflet/Chart.js) integration | Developer C | 2024-10-10 | 2024-10-22 | Completed |
| 061CSE215-D | Database setup script and deployment config | Developer D | 2024-10-08 | 2024-10-12 | Completed |

## 15. Conclusion
The project demonstrates layered Spring Boot architecture with clear separation of concerns, strategy-based extensibility for coverage and difficulty models, and robust GPX parsing plus metric computations. Strengths include modular utilities, clean REST API design, and front-end visualization. Limitations: single-table schema without user authentication, reliance on in-memory route cache, and simplified difficulty heuristic. Future improvements could add authenticated users, richer ERD (route versions, checkpoints), more advanced classification (machine learning), and offline validation for large GPX datasets.

## 16. References
- Spring Boot Reference Documentation 3.3.x
- Java Platform Standard Edition 17 Documentation
- GPX 1.1 Schema Specification (Topografix)
- MySQL 8.0 Reference Manual
- Leaflet and Chart.js official guides for front-end visualization
