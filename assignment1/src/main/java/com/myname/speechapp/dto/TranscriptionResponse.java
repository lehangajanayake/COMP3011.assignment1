package com.lehangajanayake.speechapp.dto;

/**
 * JSON response returned after an audio transcription attempt.
 */
public record TranscriptionResponse(String text) {
}