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

       return ctx.select(
                field("spb.balance_id").as("balance_id"),
                field("spb.remaining").as("remaining"),
                field("spb.consumed").as("consumed"),
                field("spb.package_id").as("package_id")
        )
                .from(table("service_package_balance").as("spb"))
                .join(table("service_package").as("sp")).on(field("spb.package_id").eq(field("sp.package_id")))
                .where(field("spb.contract_id").eq(contractId))
                .and(field("sp.service_type").eq(serviceType))
                .and(field("spb.cycle_start").le(startTime.toLocalDate()))
                .and(field("spb.cycle_end").ge(startTime.toLocalDate()))
                .limit(1) 
                .fetchOne();
    }
    

    @Override
    public Record fetchPricingMatrix(DSLContext ctx, int contractId, short serviceType) {
       String rorField = "rp.voice_ror";
        if (serviceType == 2) rorField = "rp.sms_ror";
        if (serviceType == 3) rorField = "rp.data_ror";

        return ctx.select(
                field("sp.package_id").as("package_id"),
                field(rorField).as("rate_per_unit"),
                field("sp.is_external").as("is_external")
        )
                .from(table("contract").as("c"))
                .join(table("rateplan").as("rp")).on(field("c.rateplan_id").eq(field("rp.rateplan_id")))
                .join(table("service_package").as("sp")).on(field("rp.rateplan_id").eq(field("sp.rateplan_id")))
                .where(field("c.contract_id").eq(contractId))
                .and(field("sp.service_type").eq(serviceType))
                .limit(1)
                .fetchOne();
    }

    @Override
    public void updateWalletBalance(DSLContext ctx, int balanceId, double consumedDelta, double remainingNew) {
        ctx.update(table("service_package_balance"))
                .set(field("consumed"), field("consumed", Double.class).add(consumedDelta))
                .set(field("remaining"), remainingNew)
                .set(field("last_updated"), currentTimestamp())
                .where(field("balance_id").eq(balanceId))
                .execute();
    }

}