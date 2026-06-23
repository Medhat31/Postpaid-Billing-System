package com.billing.rating_engine.processor;

import com.billing.rating_engine.repository.IRatedCdrRepository;
import com.billing.rating_engine.repository.IWalletRepository;
import org.jooq.DSLContext;
import org.jooq.Record;

/**
 *
 * @author mfathy
 */
public class VoiceSMSProcessor implements ICdrProcessor {
    
    private final IWalletRepository walletRepo;
    private final IRatedCdrRepository ratedRepo;

    public VoiceSMSProcessor(IWalletRepository walletRepo, IRatedCdrRepository ratedRepo) {
        this.walletRepo = walletRepo;
        this.ratedRepo = ratedRepo;
    }

    @Override
    public void process(DSLContext ctx, Record cdr, Record pricing, Record wallet) {
        double quantity = cdr.get("quantity", Double.class);
        int contractId = cdr.get("contract_id", Integer.class);
        long cdrId = cdr.get("cdr_id", Long.class);
        short serviceType = cdr.get("service_type", Short.class);
        
        int packageId = pricing.get("package_id", Integer.class);
        double ratePerUnit = pricing.get("rate_per_unit", Double.class);

        double fuConsumed = 0;
        double billableUnits = quantity;
        double chargedAmount = 0;

        if (wallet != null) {
            double remaining = wallet.get("remaining", Double.class);
            int balanceId = wallet.get("balance_id", Integer.class);

            if (remaining >= quantity) {
                fuConsumed = quantity;
                billableUnits = 0;
                walletRepo.updateWalletBalance(ctx, balanceId, quantity, remaining - quantity);
            } else {
                fuConsumed = remaining;
                billableUnits = quantity - remaining;
                walletRepo.updateWalletBalance(ctx, balanceId, remaining, 0.0);
            }
        }

        if (billableUnits > 0) {
            chargedAmount = billableUnits * ratePerUnit;
        }

        ratedRepo.insertRatedRecord(ctx, cdrId, contractId, packageId, serviceType, fuConsumed, billableUnits, chargedAmount);
    }
    
}
