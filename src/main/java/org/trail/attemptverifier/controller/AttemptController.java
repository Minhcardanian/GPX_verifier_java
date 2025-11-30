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

import java.util.List;
import java.util.Optional;

/**
 * REST API for attempt verification & querying.
 * Clean layering:
 *   Controller → Service → Repository
 */
@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptVerifierService attemptVerifierService;
    private final AttemptRepository attemptRepository;

    public AttemptController(AttemptVerifierService attemptVerifierService,
                             AttemptRepository attemptRepository) {
        this.attemptVerifierService = attemptVerifierService;
        this.attemptRepository = attemptRepository;
    }

    // ------------------------------------------------------------
    // POST /api/attempts/upload
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
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Runner ID cannot be empty."));
        }
        if (gpxFile == null || gpxFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("GPX file is required."));
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
    // GET /api/attempts
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
    // GET /api/attempts/{id}/track
    // Returns parsed TrackPoint list for mapping (JSON).
    // Still useful for debugging and non-GPX clients.
    // ------------------------------------------------------------
    @GetMapping(value = "/{id}/track", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrackPoint>> getAttemptTrack(@PathVariable("id") Long id) {
        List<TrackPoint> pts = attemptVerifierService.loadAttemptTrack(id);
        // Always 200; UI can decide how to handle empty list
        return ResponseEntity.ok(pts);
    }

    // ------------------------------------------------------------
    // GET /api/attempts/{id}/gpx
    // Raw GPX bytes endpoint for leaflet-gpx plugin.
    // ------------------------------------------------------------
    @GetMapping(value = "/{id}/gpx", produces = "application/gpx+xml")
    public ResponseEntity<byte[]> getAttemptGpx(@PathVariable("id") Long id) {
        Optional<Attempt> found = attemptRepository.findById(id);

        if (found.isEmpty() || found.get().getGpxData() == null) {
            // 404 with empty body is fine; leaflet-gpx will trigger its error handler
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] gpx = found.get().getGpxData();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML); // OK for GPX
        headers.setContentLength(gpx.length);

        return new ResponseEntity<>(gpx, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------
    // Simple error DTO
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
