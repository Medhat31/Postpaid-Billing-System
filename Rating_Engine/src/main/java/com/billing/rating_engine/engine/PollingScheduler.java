package com.billing.rating_engine.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PollingScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final IRatingEngine ratingEngine;

    public PollingScheduler(IRatingEngine ratingEngine) {
        this.ratingEngine = ratingEngine;
    }

    public void start() {
        // Executes the rating cycle every 2 seconds
        scheduler.scheduleAtFixedRate(ratingEngine::startRatingCycle, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}