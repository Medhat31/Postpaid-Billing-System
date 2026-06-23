package com.billing.rating_engine.engine;

import com.billing.rating_engine.config.DatabaseConfig;
import org.jooq.DSLContext;

/**
 *
 * @author mfathy
 */
public class PollingScheduler implements Runnable {
    private final IRatingEngine engine;
    private boolean running = true;

    public PollingScheduler(IRatingEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run() {
        DSLContext ctx = DatabaseConfig.getDSLContext();
        while (running) {
            try {
                engine.processNextBatch(ctx);
                Thread.sleep(2000); 
            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Database polling iteration error: " + e.getMessage());
            }
        }
    }
}
