package com.lehangajanayake.speechapp.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lehangajanayake.speechapp.dto.ErrorResponse;
import com.lehangajanayake.speechapp.dto.ShutdownResponse;
import com.lehangajanayake.speechapp.dto.UptimeResponse;
import com.lehangajanayake.speechapp.service.StatsService;

/**
 * Exposes placeholder operational endpoints for uptime, statistics, and shutdown.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final Instant applicationStartedAt;
    private final StatsService statsService;
    private final ApplicationContext applicationContext;
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean();

    public AdminController(StatsService statsService, ApplicationContext applicationContext) {
        this.applicationStartedAt = Instant.now();
        this.statsService = statsService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/uptime")
    public ResponseEntity<UptimeResponse> uptime() {
        Instant utcNow = Instant.now();
        Duration uptime = Duration.between(applicationStartedAt, utcNow);
        double uptimeSeconds = uptime.toNanos() / 1_000_000_000.0;

        return ResponseEntity.ok(new UptimeResponse(applicationStartedAt, utcNow, uptimeSeconds));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "requestsReceived", statsService.getRequestsReceived(),
                "requestsSucceeded", statsService.getRequestsSucceeded(),
                "requestsFailed", statsService.getRequestsFailed()));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdown() {
        if (!shutdownInProgress.compareAndSet(false, true)) {
            return ResponseEntity.status(409).body(new ErrorResponse(
                    Instant.now(),
                    409,
                    "Conflict",
                    "Graceful shutdown is already in progress.",
                    "/api/v1/admin/shutdown"));
        }

        // TODO: Add authentication/authorization before enabling this admin endpoint.
        Thread.startVirtualThread(() -> SpringApplication.exit(applicationContext));
        return ResponseEntity.accepted().body(new ShutdownResponse("Graceful shutdown requested."));
    }
}