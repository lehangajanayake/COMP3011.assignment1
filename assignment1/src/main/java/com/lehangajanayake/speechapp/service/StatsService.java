package com.lehangajanayake.speechapp.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final AtomicLong inputTokens = new AtomicLong();
    private final AtomicLong outputTokens = new AtomicLong();

    public void recordTokenUsage(long inputTokens, long outputTokens) {
        this.inputTokens.addAndGet(inputTokens);
        this.outputTokens.addAndGet(outputTokens);
    }
    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }
}
