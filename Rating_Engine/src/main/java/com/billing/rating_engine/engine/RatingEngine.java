package com.billing.rating_engine.engine;

import com.billing.rating_engine.config.DatabaseConfig;
import com.billing.rating_engine.processor.DataProcessor;
import com.billing.rating_engine.processor.ICdrProcessor;
import com.billing.rating_engine.processor.VoiceSMSProcessor;
import com.billing.rating_engine.repository.CdrRepository;
import com.billing.rating_engine.repository.ICdrRepository;
import com.billing.rating_engine.repository.IWalletRepository;
import com.billing.rating_engine.repository.WalletRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

public class RatingEngine implements IRatingEngine {
    private final ICdrRepository cdrRepo;
    private final ICdrProcessor voiceSmsProcessor;
    private final ICdrProcessor dataProcessor;

    public RatingEngine() {
        this.cdrRepo = new CdrRepository();
        IWalletRepository walletRepo = new WalletRepository();
        this.voiceSmsProcessor = new VoiceSMSProcessor(walletRepo, cdrRepo);
        this.dataProcessor = new DataProcessor(walletRepo, cdrRepo);
    }

    @Override
    public void startRatingCycle() {
        try {
            
            DSLContext ctx = DatabaseConfig.getDSLContext();
            Result<Record> unratedCdrs = cdrRepo.fetchUnratedCdrs(ctx, 100); 

            if (unratedCdrs.isEmpty()) {
                return;
            }

            System.out.println("Processing batch of " + unratedCdrs.size() + " CDRs...");

            for (Record cdr : unratedCdrs) {
                short serviceType = cdr.get("service_type", Short.class);
                try {
                    if (serviceType == 1 || serviceType == 2) {
                        voiceSmsProcessor.process(ctx, cdr);
                    } else if (serviceType == 3) {
                        dataProcessor.process(ctx, cdr);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to rate CDR ID: " + cdr.get("cdr_id") + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}