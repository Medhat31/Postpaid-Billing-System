package com.billing.rating_engine.main;

// Import your newly created root packages
import com.billing.rating_engine.repository.*;
import com.billing.rating_engine.engine.IRatingEngine;
import com.billing.rating_engine.engine.RatingEngine;
import com.billing.rating_engine.engine.PollingScheduler;
import com.billing.rating_engine.config.DatabaseConfig;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting BSCS Batch Production Processing Engine Core...");
        
        // 1. Initialize Core Structural Repositories
        ICdrRepository cdrRepo = new CdrRepository();
        IWalletRepository walletRepo = new WalletRepository();
        IRatedCdrRepository ratedRepo = new RatedCdrRepository();

        // 2. Inject repositories into the Rating Engine Implementation
        IRatingEngine engine = new RatingEngine(cdrRepo, walletRepo, ratedRepo);
        
        // 3. Pass the engine to the background scheduler loop
        PollingScheduler scheduler = new PollingScheduler(engine);
        Thread engineThread = new Thread(scheduler);
        
        // 4. Setup a simple JVM graceful shutdown hook to clean up HikariCP
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConfig.shutdown();
        }));

        // 5. Fire up the background thread
        engineThread.start();
    }
}