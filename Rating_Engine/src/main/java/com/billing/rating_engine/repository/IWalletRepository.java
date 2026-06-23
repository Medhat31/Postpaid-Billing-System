package com.billing.rating_engine.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import java.time.LocalDateTime;

public interface IWalletRepository {
    Record fetchActiveWallet(DSLContext ctx, int contractId, LocalDateTime startTime, short serviceType);
    Record fetchPricingMatrix(DSLContext ctx, int contractId, short serviceType);
    void updateWalletBalance(DSLContext ctx, int balanceId, double consumedDelta, double remainingNew);
}