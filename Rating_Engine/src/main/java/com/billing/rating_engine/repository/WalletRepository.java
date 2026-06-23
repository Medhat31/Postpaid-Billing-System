package com.billing.rating_engine.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import static org.jooq.impl.DSL.*;
import java.time.LocalDateTime;

/**
 *
 * @author mfathy
 */
public class WalletRepository implements IWalletRepository {

    @Override
    public Record fetchActiveWallet(DSLContext ctx, int contractId, LocalDateTime startTime, short serviceType) { 

        String fuType = "";
        switch (serviceType) {
            case 1 -> fuType = "voice";
            case 2 -> fuType = "sms";
            case 3 -> fuType = "data";
            default -> {
            }
        }

        return ctx.select(
                field("fb.balance_id").as("balance_id"),
                field("fb.remaining").as("remaining"),
                field("fb.consumed").as("consumed"),
                field("fb.fu_id").as("fu_id")
        )
                .from(table("fu_balance").as("fb"))
                .join(table("free_unit").as("fu")).on(field("fb.fu_id").eq(field("fu.fu_id")))
                .where(field("fb.contract_id").eq(contractId))
                .and(field("fu.fu_type").eq(field("cast(? as fu_type)", String.class, fuType)))
                .and(field("fb.cycle_start").le(startTime.toLocalDate()))
                .and(field("fb.cycle_end").ge(startTime.toLocalDate()))
                .limit(1) 
                .fetchOne();
    }

    @Override
    public Record fetchPricingMatrix(DSLContext ctx, int contractId, short serviceType) {
        return ctx.select(field("sp.package_id").as("package_id"),
                field("sp.rate_per_unit").as("rate_per_unit"),
                field("sp.is_external").as("is_external")
        )
                .from(table("contract").as("c"))
                .join(table("service_package").as("sp")).on(field("c.rateplan_id").eq(field("sp.rateplan_id")))
                .where(field("c.contract_id").eq(contractId))
                .and(field("sp.service_type").eq(serviceType))
                .limit(1)
                .fetchOne();
    }

    @Override
    public void updateWalletBalance(DSLContext ctx, int balanceId, double consumedDelta, double remainingNew) {
        ctx.update(table("fu_balance"))
                .set(field("consumed"), field("consumed", Double.class).add(consumedDelta))
                .set(field("remaining"), remainingNew)
                .set(field("last_updated"), currentTimestamp())
                .where(field("balance_id").eq(balanceId))
                .execute();
    }
}
