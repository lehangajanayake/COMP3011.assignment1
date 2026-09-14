package com.myname.speechapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;

class TranscriptionServiceTest {

    @Test
    void sendsAudioAndModelToStubbedSpeechProvider() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        String apiKey = System.getenv().getOrDefault("OPENAI_API_KEY", "");
        TranscriptionService service = new TranscriptionService(client, apiKey);

        server.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer " + apiKey))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("gpt-4o-mini-transcribe")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sample audio")))
                .andRespond(withSuccess("{\"text\":\"stubbed transcript\"}", MediaType.APPLICATION_JSON));

        MockMultipartFile audio = new MockMultipartFile(
                "audio", "sample.webm", "audio/webm", "sample audio".getBytes(StandardCharsets.UTF_8));

        assertThat(service.transcribe(audio).text()).isEqualTo("stubbed transcript");
        server.verify();
    }
}
