package com.myname.speechapp.controller;

import com.myname.speechapp.service.StatsService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes placeholder operational endpoints for uptime, statistics, and shutdown.
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final Instant applicationStartedAt;
    private final StatsService statsService;
    private final ApplicationContext applicationContext;

    public StatusController(StatsService statsService, ApplicationContext applicationContext) {
        this.applicationStartedAt = Instant.now();
        this.statsService = statsService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/uptime")
    public ResponseEntity<Map<String, Object>> uptime() {
        return ResponseEntity.ok(Map.of(
                "startedAt", applicationStartedAt,
                "uptimeSeconds", Duration.between(applicationStartedAt, Instant.now()).toSeconds()));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "requestsReceived", statsService.getRequestsReceived(),
                "requestsSucceeded", statsService.getRequestsSucceeded(),
                "requestsFailed", statsService.getRequestsFailed()));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown() {
        // TODO: Add authentication/authorization before enabling this admin endpoint.
        SpringApplication.exit(applicationContext);
        return ResponseEntity.accepted().build();
    }
}