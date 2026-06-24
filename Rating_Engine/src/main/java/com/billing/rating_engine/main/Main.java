package com.billing.rating_engine.main;

import com.billing.rating_engine.engine.PollingScheduler;
import com.billing.rating_engine.engine.RatingEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Postpaid Billing System...");
        
        RatingEngine engine = new RatingEngine();
        PollingScheduler scheduler = new PollingScheduler(engine);
        
        scheduler.start();
        
        // Add a shutdown hook to cleanly close database connections and threads if the app is killed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("System shutdown initiated...");
            scheduler.stop();
        }));
    }
}