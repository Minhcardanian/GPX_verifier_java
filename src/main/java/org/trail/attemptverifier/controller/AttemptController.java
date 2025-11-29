package org.trail.attemptverifier.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.trail.attemptverifier.model.Attempt;
import org.trail.attemptverifier.repository.AttemptRepository;
import org.trail.attemptverifier.service.AttemptVerifierService;

import java.util.List;

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

    @PostMapping(
            path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Attempt> uploadAttempt(
            @RequestParam("runnerId") String runnerId,
            @RequestParam("file") MultipartFile gpxFile
    ) {
        Attempt attempt = attemptVerifierService.verifyAttempt(gpxFile, runnerId);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping
    public List<Attempt> listAttempts() {
        return attemptRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attempt> getAttempt(@PathVariable("id") Long id) {
        return attemptRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
