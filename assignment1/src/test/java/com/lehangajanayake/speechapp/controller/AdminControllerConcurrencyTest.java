package com.lehangajanayake.speechapp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.mock;

class AdminControllerConcurrencyTest {

    @Test
    void onlyOneConcurrentShutdownRequestIsAccepted() throws Exception {
        AdminController controller = new AdminController(mock(ApplicationContext.class));
        ExecutorService executor = Executors.newFixedThreadPool(256);
        List<Future<ResponseEntity<?>>> requests = new ArrayList<>();

        try {
            for (int requestNumber = 0; requestNumber < 256; requestNumber++) {
                requests.add(executor.submit(controller::shutdown));
            }

            int accepted = 0;
            int conflicts = 0;
            for (Future<ResponseEntity<?>> request : requests) {
                if (request.get().getStatusCode().value() == 202) {
                    accepted++;
                } else if (request.get().getStatusCode().value() == 409) {
                    conflicts++;
                }
            }

            assertThat(accepted).isEqualTo(1);
            assertThat(conflicts).isEqualTo(255);
        } finally {
            executor.shutdownNow();
        }
    }
}