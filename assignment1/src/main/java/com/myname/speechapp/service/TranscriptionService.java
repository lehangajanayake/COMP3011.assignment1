package com.myname.speechapp.service;

import com.myname.speechapp.dto.TranscriptionResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the application boundary for turning uploaded audio into text with OpenAI.
 */
@Service
public class TranscriptionService {

    private static final String TRANSCRIPTION_MODEL = "gpt-4o-mini-transcribe";
    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);

    private final RestClient openAiRestClient;
    private final String apiKey;

    public TranscriptionService(RestClient openAiRestClient, @Value("${openai.api-key}") String apiKey) {
        this.openAiRestClient = openAiRestClient;
        this.apiKey = apiKey;
    }

    public TranscriptionResponse transcribe(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("An audio file is required");
        }

        long startedAt = System.nanoTime();
        try {
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("model", TRANSCRIPTION_MODEL);
            form.add("file", audioPart(audio));

            TranscriptionResponse response = openAiRestClient.post()
                    .uri("/v1/audio/transcriptions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(TranscriptionResponse.class);

            if (response == null || response.text() == null) {
                throw new IllegalStateException("The transcription provider returned no text");
            }

            log.info("Speech transcription completed in {} ms", elapsedMillis(startedAt));
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the uploaded audio", exception);
        }
    }

    private ByteArrayResource audioPart(MultipartFile audio) throws IOException {
        String filename = audio.getOriginalFilename() == null
                ? "recording.webm"
                : audio.getOriginalFilename();
        MediaType contentType = audio.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(audio.getContentType());

        return new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}