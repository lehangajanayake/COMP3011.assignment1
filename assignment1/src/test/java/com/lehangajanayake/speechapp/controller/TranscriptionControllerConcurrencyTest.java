package com.lehangajanayake.speechapp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.lehangajanayake.speechapp.dto.TranscriptionResponse;
import com.lehangajanayake.speechapp.service.TranscriptionService;

class TranscriptionControllerConcurrencyTest {

    private static final int REQUEST_COUNT = 256;

    @Test
    void acceptsMoreThan200SimultaneousBlockingRequestsWithoutRacingCounters() throws Exception {
        TranscriptionService transcriptionService = mock(TranscriptionService.class);
        AtomicInteger entered = new AtomicInteger();
        CountDownLatch allRequestsEnteredService = new CountDownLatch(1);
        CountDownLatch releaseRequests = new CountDownLatch(1);

        when(transcriptionService.transcribe(any())).thenAnswer(invocation -> {
            if (entered.incrementAndGet() == REQUEST_COUNT) {
                allRequestsEnteredService.countDown();
            }
            releaseRequests.await(15, TimeUnit.SECONDS);
            return new TranscriptionResponse("stubbed transcript");
        });

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TranscriptionController(transcriptionService))
                .build();
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        List<Future<?>> requests = new ArrayList<>(REQUEST_COUNT);
        MockMultipartFile audio = new MockMultipartFile("audio", "sample.webm", "audio/webm", new byte[]{1, 2, 3});

        try {
            for (int requestNumber = 0; requestNumber < REQUEST_COUNT; requestNumber++) {
                requests.add(executor.submit(() -> mockMvc.perform(
                        multipart("/api/transcribe").file(audio)).andExpect(status().isOk()).andReturn()));
            }

            assertThat(allRequestsEnteredService.await(15, TimeUnit.SECONDS)).isTrue();
            releaseRequests.countDown();
            for (Future<?> request : requests) {
                request.get(15, TimeUnit.SECONDS);
            }
        } finally {
            releaseRequests.countDown();
            executor.shutdownNow();
        }
    }
}
