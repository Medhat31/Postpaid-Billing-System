package com.billing.rating_engine.engine;

import com.billing.rating_engine.processor.ICdrProcessor;
import com.billing.rating_engine.processor.DataProcessor;
import com.billing.rating_engine.processor.VoiceSMSProcessor;
import com.billing.rating_engine.repository.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mfathy
 */
public class RatingEngine implements IRatingEngine {
    private final ICdrRepository cdrRepo;
    private final IWalletRepository walletRepo;
    private final Map<Short, ICdrProcessor> processors = new HashMap<>();
    
    public RatingEngine(ICdrRepository cdrRepo, IWalletRepository walletRepo, IRatedCdrRepository ratedRepo) {
        this.cdrRepo = cdrRepo;
        this.walletRepo = walletRepo;

        ICdrProcessor voiceSms = new VoiceSMSProcessor(walletRepo, ratedRepo);
        this.processors.put((short) 1, voiceSms); 
        this.processors.put((short) 2, voiceSms); 
        this.processors.put((short) 3, new DataProcessor(walletRepo, ratedRepo)); 
    }
    
    @Override
    public void processNextBatch(DSLContext mainCtx) {
        Result<Record> batch = cdrRepo.fetchUnratedCdrs(mainCtx, 100);
        if (batch.isEmpty()) return;

        mainCtx.transaction(configuration -> {
            DSLContext tx = org.jooq.impl.DSL.using(configuration);

            for (Record cdr : batch) {
                int contractId = cdr.get("contract_id", Integer.class);
                short serviceType = cdr.get("service_type", Short.class);
                LocalDateTime startTime = cdr.get("start_time", LocalDateTime.class);

                Record pricing = walletRepo.fetchPricingMatrix(tx, contractId, serviceType);
                if (pricing == null) continue; 

                Record wallet = walletRepo.fetchActiveWallet(tx, contractId, startTime, serviceType);
                ICdrProcessor processor = processors.get(serviceType);

                if (processor != null) {
                    processor.process(tx, cdr, pricing, wallet);
                    cdrRepo.markAsRated(tx, cdr.get("cdr_id", Long.class));
                }
            }
        });
        System.out.println("Processed batch run for " + batch.size() + " unrated CDR records.");
    }
    
}
