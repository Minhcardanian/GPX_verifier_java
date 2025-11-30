package org.trail.attemptverifier.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.trail.attemptverifier.model.Attempt;
import org.trail.attemptverifier.model.TrackPoint;
import org.trail.attemptverifier.repository.AttemptRepository;
import org.trail.attemptverifier.service.AttemptVerifierService;
import org.trail.attemptverifier.util.GpxParser;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

/**
 * REST API for attempt verification, querying, and GPX retrieval.
 * Adds endpoints for map visualization (Leaflet-friendly).
 */
@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptVerifierService attemptVerifierService;
    private final AttemptRepository attemptRepository;
    private final GpxParser gpxParser;

    public AttemptController(AttemptVerifierService attemptVerifierService,
                             AttemptRepository attemptRepository,
                             GpxParser gpxParser) {
        this.attemptVerifierService = attemptVerifierService;
        this.attemptRepository = attemptRepository;
        this.gpxParser = gpxParser;
    }

    // ------------------------------------------------------------
    // Upload + verify GPX attempt
    // ------------------------------------------------------------
    @PostMapping(
            path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadAttempt(
            @RequestParam("runnerId") String runnerId,
            @RequestParam("file") MultipartFile gpxFile
    ) {
        if (runnerId == null || runnerId.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Runner ID cannot be empty."));
        }
        if (gpxFile == null || gpxFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("GPX file is required."));
        }

        try {
            Attempt attempt = attemptVerifierService.verifyAttempt(gpxFile, runnerId.trim());
            return ResponseEntity.ok(attempt);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Verification failed: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------
    // List + filter attempts
    // ------------------------------------------------------------
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Attempt>> listAttempts(
            @RequestParam(value = "runner", required = false) String runnerId,
            @RequestParam(value = "result", required = false) String result
    ) {
        boolean filterRunner = runnerId != null && !runnerId.isBlank();
        boolean filterResult = result != null && !result.isBlank();

        List<Attempt> list;

        if (!filterRunner && !filterResult) {
            list = attemptRepository.findAll();

        } else if (filterRunner && filterResult) {
            list = attemptRepository.findByRunnerIdAndResult(
                    runnerId.trim(),
                    result.trim().toUpperCase()
            );

        } else if (filterRunner) {
            list = attemptRepository.findByRunnerId(runnerId.trim());

        } else {
            list = attemptRepository.findByResult(result.trim().toUpperCase());
        }

        return ResponseEntity.ok(list);
    }

    // ------------------------------------------------------------
    // GET /api/attempts/{id}
    // ------------------------------------------------------------
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAttempt(@PathVariable("id") Long id) {
        Optional<Attempt> found = attemptRepository.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Attempt ID " + id + " not found."));
        }

        return ResponseEntity.ok(found.get());
    }

    // ------------------------------------------------------------
    // NEW ENDPOINT:
    // GET raw GPX for map viewer
    // ------------------------------------------------------------
    @GetMapping(value = "/{id}/gpx", produces = "application/gpx+xml")
    public ResponseEntity<?> getGpxRaw(@PathVariable("id") Long id) {
        Optional<Attempt> found = attemptRepository.findById(id);

        if (found.isEmpty() || found.get().getGpxData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No GPX available for attempt " + id));
        }

        byte[] gpx = found.get().getGpxData();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/gpx+xml"));
        headers.setContentLength(gpx.length);
        headers.set("Content-Disposition", "inline; filename=attempt-" + id + ".gpx");

        return new ResponseEntity<>(gpx, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------
    // NEW ENDPOINT:
    // GET parsed track as JSON (lat/lon/elev/time)
    // For Leaflet polylines
    // ------------------------------------------------------------
    @GetMapping(value = "/{id}/track", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getParsedTrack(@PathVariable("id") Long id) {

        Optional<Attempt> found = attemptRepository.findById(id);

        if (found.isEmpty() || found.get().getGpxData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No GPX available for attempt " + id));
        }

        byte[] gpx = found.get().getGpxData();

        List<TrackPoint> points = gpxParser.parse(new ByteArrayInputStream(gpx));

        return ResponseEntity.ok(points);
    }

    // ------------------------------------------------------------
    // Error DTO
    // ------------------------------------------------------------
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
