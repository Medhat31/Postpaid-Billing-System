package com.billing.rating_engine.processor;

import com.billing.rating_engine.repository.IRatedCdrRepository;
import com.billing.rating_engine.repository.IWalletRepository;
import org.jooq.DSLContext;
import org.jooq.Record;

/**
 *
 * @author mfathy
 */
public class DataProcessor implements ICdrProcessor {
    private final IWalletRepository walletRepo;
    private final IRatedCdrRepository ratedRepo;

    public DataProcessor(IWalletRepository walletRepo, IRatedCdrRepository ratedRepo) {
        this.walletRepo = walletRepo;
        this.ratedRepo = ratedRepo;
    }

    @Override
    public void process(DSLContext ctx, Record cdr, Record pricing, Record wallet) {
        double bytesVolume = cdr.get("quantity", Double.class); 
        int contractId = cdr.get("contract_id", Integer.class);
        long cdrId = cdr.get("cdr_id", Long.class);
        short serviceType = cdr.get("service_type", Short.class);
        int externalFeePiasters = cdr.get("external_fee_piasters", Integer.class);
        int packageId = pricing.get("package_id", Integer.class);

        double fuConsumed = 0;
        double billableUnits = bytesVolume;
        double chargedAmount = 0;

        if (wallet != null) {
            double remaining = wallet.get("remaining", Double.class);
            int balanceId = wallet.get("balance_id", Integer.class);

            if (remaining >= bytesVolume) {
                fuConsumed = bytesVolume;
                billableUnits = 0;
                walletRepo.updateWalletBalance(ctx, balanceId, bytesVolume, remaining - bytesVolume);
            } else {
                fuConsumed = remaining;
                billableUnits = bytesVolume - remaining;
                walletRepo.updateWalletBalance(ctx, balanceId, remaining, 0.0);
            }
        }

        if (billableUnits > 0 && externalFeePiasters > 0) {
            chargedAmount = externalFeePiasters / 100.0;
        }

        ratedRepo.insertRatedRecord(ctx, cdrId, contractId, packageId, serviceType, fuConsumed, billableUnits, chargedAmount);
    }
}
