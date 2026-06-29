package com.billing.rating_engine.processor;

import com.billing.rating_engine.repository.ICdrRepository;
import com.billing.rating_engine.repository.IWalletRepository;
import java.math.BigDecimal;
import org.jooq.DSLContext;
import org.jooq.Record;
import java.time.LocalDateTime;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class DataProcessor implements ICdrProcessor {
    private final IWalletRepository walletRepo;
    private final ICdrRepository cdrRepo;

    public DataProcessor(IWalletRepository walletRepo, ICdrRepository cdrRepo) {
        this.walletRepo = walletRepo;
        this.cdrRepo = cdrRepo;
    }

    @Override
    public void process(DSLContext ctx, Record cdr) {
       long cdrId = cdr.get("cdr_id", Long.class);
        int contractId = cdr.get("contract_id", Integer.class);
        short serviceType = cdr.get("service_type", Short.class); 
        
       double quantityBytes = cdr.get("quantity", Double.class);
       double quantityMB = quantityBytes / (1024.0 * 1024.0);
       
        LocalDateTime startTime = cdr.get("start_time", LocalDateTime.class);
        
        Integer externalFeePiasters = cdr.get("external_fee_piasters", Integer.class);
        double externalFeeEgp = (externalFeePiasters != null ? externalFeePiasters : 0) / 100.0;

        Record wallet = walletRepo.fetchActiveWallet(ctx, contractId, startTime, serviceType);
        Record pricing = walletRepo.fetchPricingMatrix(ctx, contractId, serviceType);

        double ratePerUnit = pricing != null ? pricing.get("rate_per_unit", Double.class) : 0.0;
        double chargedAmount = 0.0;

        if (wallet != null) {
            int balanceId = wallet.get("balance_id", Integer.class);
            double remaining = wallet.get("remaining", Double.class);

            if (remaining >= quantityMB) {
                walletRepo.updateWalletBalance(ctx, balanceId, quantityMB, remaining - quantityMB);
            } else {
                double outOfQuota = quantityMB - remaining;
                walletRepo.updateWalletBalance(ctx, balanceId, remaining, 0.0);
                chargedAmount = outOfQuota * ratePerUnit;
            }
        } else {
            chargedAmount = quantityMB * ratePerUnit;
        }

        chargedAmount += externalFeeEgp;
        cdrRepo.updateCdrAfterRating(ctx, cdrId, chargedAmount);
        
          if (chargedAmount > 0.0) {
            ctx.update(table("contract"))
                    .set(field("unbilled_amount"), field("unbilled_amount", BigDecimal.class).add(chargedAmount))
                    .where(field("contract_id").eq(contractId))
                    .execute();
        }    
    } 
}
