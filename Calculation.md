# GPX Calculation Flowchart

mermaid
```
f%% GPX Calculation Flowchart
flowchart TD
    A[Attempt GPX bytes] --> B{Parse GPX}
    B -->|trkpt nodes| C[TrackPoint list]

    C --> D["TrackMetrics<br>• Total distance (km)<br>• Elevation gain (m)"]
    C --> E["Coverage calculation<br>• Downsample to 5k pts<br>• Sliding nearest neighbor<br>• Within tolerance?"]
    C --> F["Max deviation<br>• Same sliding search<br>• Record farthest distance"]

    D --> G["Difficulty model<br>Base workload = distance + elevation/100"]
    E --> G
    F --> G

    G --> H{Classify attempt}
    H -->|High coverage & low deviation| I[VERIFIED]
    H -->|Partial coverage or higher deviation| J[FLAGGED]
    H -->|Low coverage or very high deviation| K[REJECTED]

```

**Notes**
- Parsing is DOM-based, extracting latitude/longitude plus optional elevation and time for each `<trkpt>`.
- Distance uses Haversine between consecutive points; elevation gain sums positive deltas.
- Coverage and max deviation share the downsampling + sliding nearest-neighbor strategy against the official route.
- Difficulty score combines distance, elevation, coverage bonus, and deviation penalty, floored at zero.
- The classifier uses coverage and deviation thresholds to determine the final status.