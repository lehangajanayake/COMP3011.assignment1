package com.lehangajanayake.speechapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.lehangajanayake.speechapp.dto.TranscriptionResponse;
import com.lehangajanayake.speechapp.service.TranscriptionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "openai.api-key=test-only-placeholder")
class EmbeddedServerConcurrencyTest {

    private static final int REQUEST_COUNT = 256;

    @LocalServerPort
    private int port;

    @MockitoBean
    private TranscriptionService transcriptionService;

    @Test
    void handles256RealHttpRequestsOnVirtualThreads() throws Exception {
        AtomicInteger entered = new AtomicInteger();
        AtomicBoolean usedVirtualThread = new AtomicBoolean();
        CountDownLatch allRequestsEntered = new CountDownLatch(1);
        CountDownLatch releaseRequests = new CountDownLatch(1);

        when(transcriptionService.transcribe(any())).thenAnswer(invocation -> {
            usedVirtualThread.set(Thread.currentThread().isVirtual());
            if (entered.incrementAndGet() == REQUEST_COUNT) {
                allRequestsEntered.countDown();
            }
            releaseRequests.await(15, TimeUnit.SECONDS);
            return new TranscriptionResponse("stubbed transcript");
        });

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        List<Future<?>> requests = new ArrayList<>();

        try {
            for (int requestNumber = 0; requestNumber < REQUEST_COUNT; requestNumber++) {
                requests.add(executor.submit(() -> client.post()
                        .uri("/api/transcribe")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(audioForm())
                        .retrieve()
                        .toBodilessEntity()));
            }

            assertThat(allRequestsEntered.await(15, TimeUnit.SECONDS)).isTrue();
            assertThat(usedVirtualThread).isTrue();
            releaseRequests.countDown();

            for (Future<?> request : requests) {
                request.get(15, TimeUnit.SECONDS);
            }
        } finally {
            releaseRequests.countDown();
            executor.shutdownNow();
        }
    }

    private MultiValueMap<String, Object> audioForm() {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("audio", new ByteArrayResource(new byte[] {1, 2, 3}) {
            @Override
            public String getFilename() {
                return "sample.webm";
            }
        });
        return form;
    }
}