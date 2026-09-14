package com.lehangajanayake.speechapp.dto;

import java.time.Instant;

public record UptimeResponse(
        Instant utcServerStart,
        Instant utcNow,
        double serverUptimeSeconds) {
}