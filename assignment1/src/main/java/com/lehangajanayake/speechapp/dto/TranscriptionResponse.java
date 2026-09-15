package com.lehangajanayake.speechapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON response returned after an audio transcription attempt.
 */
public record TranscriptionResponse(String text, Usage usage) {

	public TranscriptionResponse(String text) {
		this(text, null);
	}

	public record Usage(
			@JsonProperty("input_tokens") long inputTokens,
			@JsonProperty("output_tokens") long outputTokens) {
	}
}