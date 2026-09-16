package com.lehangajanayake.speechapp.controller;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lehangajanayake.speechapp.dto.ErrorResponse;
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
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "An audio file is required.",
                "/api/transcribe"));

        } catch (org.springframework.web.client.RestClientException exception) {
            log.warn("Speech transcription provider request failed: {}", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(
                        Instant.now(),
                    HttpStatus.BAD_GATEWAY.value(),
                    HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                    "The transcription service is temporarily unavailable.",
                    "/api/transcribe"));

        } catch (Exception exception) {
            log.error("Speech transcription failed: {}", exception.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        Instant.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "Transcription failed. Please try again.",
                    "/api/transcribe"));
        }
    }
}