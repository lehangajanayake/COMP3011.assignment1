package com.lehangajanayake.speechapp.service;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.springframework.http.HttpMethod.POST;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

class TranscriptionServiceTest {

    @Test
    void sendsAudioAndModelToStubbedSpeechProvider() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        String apiKey = System.getenv().getOrDefault("OPENAI_API_KEY", "");
        StatsService statsService = new StatsService();
        TranscriptionService service = new TranscriptionService(client, apiKey, statsService);

        server.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer " + apiKey))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("gpt-4o-mini-transcribe")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sample audio")))
            .andRespond(withSuccess(
                "{\"text\":\"stubbed transcript\",\"usage\":{\"input_tokens\":12,\"output_tokens\":4}}",
                MediaType.APPLICATION_JSON));

        MockMultipartFile audio = new MockMultipartFile(
                "audio", "sample.webm", "audio/webm", "sample audio".getBytes(StandardCharsets.UTF_8));

        assertThat(service.transcribe(audio).text()).isEqualTo("stubbed transcript");
        assertThat(statsService.getInputTokens()).isEqualTo(12);
        assertThat(statsService.getOutputTokens()).isEqualTo(4);
        server.verify();
    }
}
