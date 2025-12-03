# GPX Calculation Flowchart
```mermaid
flowchart TD
    A["Attempt GPX bytes"] --> B{"Parse GPX"}
    B -->|trkpt nodes| C["TrackPoint list"]

    C --> D["Track metrics: total distance (km) and elevation gain (m)"]
    C --> E["Coverage calculation: downsample to 5k points, sliding nearest neighbor, tolerance check"]
    C --> F["Max deviation: sliding search, record farthest distance"]

    D --> G["Difficulty model: base workload = distance + elevation/100"]
    E --> G
    F --> G

    G --> H{"Classify attempt"}
    H -->|"High coverage & low deviation"| I["VERIFIED"]
    H -->|"Partial coverage or higher deviation"| J["FLAGGED"]
    H -->|"Low coverage or very high deviation"| K["REJECTED"]
```


**Notes**
- Parsing is DOM-based, extracting latitude/longitude plus optional elevation and time for each `<trkpt>`.
- Distance uses Haversine between consecutive points; elevation gain sums positive deltas.
- Coverage and max deviation share the downsampling + sliding nearest-neighbor strategy against the official route.
- Difficulty score combines distance, elevation, coverage bonus, and deviation penalty, floored at zero.
- The classifier uses coverage and deviation thresholds to determine the final status.
