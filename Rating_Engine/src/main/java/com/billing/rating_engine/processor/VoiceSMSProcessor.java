package com.billing.rating_engine.processor;

import com.billing.rating_engine.repository.ICdrRepository;
import com.billing.rating_engine.repository.IWalletRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import java.time.LocalDateTime;

public class VoiceSMSProcessor implements ICdrProcessor {
    
    private final IWalletRepository walletRepo;
    private final ICdrRepository cdrRepo;

    public VoiceSMSProcessor(IWalletRepository walletRepo, ICdrRepository cdrRepo) {
        this.walletRepo = walletRepo;
        this.cdrRepo = cdrRepo;
    }
    
    private double calculateZoneRate(String dialB, double baseRate) {
        if (dialB.startsWith("00") && !dialB.startsWith("0020")) {
            return baseRate * 50.0; // International
        } else if (dialB.startsWith("012") || dialB.startsWith("002012")) {
            return baseRate * 1.0;  // On-Net
        } else {
            return baseRate * 1.5;  // Off-Net
        }
    }

    @Override
    public void process(DSLContext ctx, Record cdr) {
       long cdrId = cdr.get("cdr_id", Long.class);
        int contractId = cdr.get("contract_id", Integer.class);
        short serviceType = cdr.get("service_type", Short.class);
        double quantity = cdr.get("quantity", Double.class);
        LocalDateTime startTime = cdr.get("start_time", LocalDateTime.class);
        String dialB = cdr.get("dial_b", String.class);

        Record wallet = walletRepo.fetchActiveWallet(ctx, contractId, startTime, serviceType);
        Record pricing = walletRepo.fetchPricingMatrix(ctx, contractId, serviceType);

        double baseRate = pricing != null ? pricing.get("rate_per_unit", Double.class) : 0.0;
        double finalRate = calculateZoneRate(dialB, baseRate);
        double chargedAmount = 0.0;

        if (wallet != null) {
            int balanceId = wallet.get("balance_id", Integer.class);
            double remaining = wallet.get("remaining", Double.class);

            if (remaining >= quantity) {
                // In-Quota: Deduct from balance, no cash charged
                walletRepo.updateWalletBalance(ctx, balanceId, quantity, remaining - quantity);
            } else {
                // Mid-Session: Drain wallet, charge cash for the rest
                double outOfQuota = quantity - remaining;
                walletRepo.updateWalletBalance(ctx, balanceId, remaining, 0.0);
                chargedAmount = outOfQuota * finalRate;
            }
        } else {
            // Out-of-Quota: No active wallet, charge pure cash
            chargedAmount = quantity * finalRate;
        }

        // Apply In-Place Rating to the CDR table
        cdrRepo.updateCdrAfterRating(ctx, cdrId, chargedAmount);
    }
    
}
