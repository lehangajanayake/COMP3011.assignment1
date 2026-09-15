package com.lehangajanayake.speechapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lehangajanayake.speechapp.dto.TranscriptionResponse;
import com.lehangajanayake.speechapp.service.TranscriptionService;

/**
 * Accepts recorded audio and delegates transcription work to the service layer.
 */
@RestController
public class TranscriptionController {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionController.class);

    private final TranscriptionService transcriptionService;
    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping(path = "/api/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> transcribe(@RequestParam("audio") MultipartFile audio) {
        try {
            TranscriptionResponse response = transcriptionService.transcribe(audio);
            log.info("Speech transcription succeeded: {}", response);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body("An audio file is required.");

        } catch (org.springframework.web.client.RestClientException exception) {
            log.warn("Speech transcription provider request failed: {}", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("The transcription service is temporarily unavailable.");

        } catch (Exception exception) {
            log.error("Speech transcription failed: {}", exception.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Transcription failed. Please try again.");
        }
    }
}