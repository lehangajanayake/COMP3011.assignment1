package com.myname.speechapp.service;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Maintains thread-safe request counters for operational status endpoints.
 */
@Service
public class StatsService {

    private final AtomicLong requestsReceived = new AtomicLong();
    private final AtomicLong requestsSucceeded = new AtomicLong();
    private final AtomicLong requestsFailed = new AtomicLong();

    public void recordRequestReceived() {
        requestsReceived.incrementAndGet();
    }

    public void recordRequestSucceeded() {
        requestsSucceeded.incrementAndGet();
    }

    public void recordRequestFailed() {
        requestsFailed.incrementAndGet();
    }

    public long getRequestsReceived() {
        return requestsReceived.get();
    }

    public long getRequestsSucceeded() {
        return requestsSucceeded.get();
    }

    public long getRequestsFailed() {
        return requestsFailed.get();
    }
}
