package com.billing.rating_engine.repository;

import org.jooq.DSLContext;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author mfathy
 */
public class RatedCdrRepository implements IRatedCdrRepository {
    @Override
    public void insertRatedRecord(DSLContext ctx, long cdrId, int contractId, int packageId, 
                                   short serviceType, double fuConsumed, double billableUnits, double chargedAmount) {
        ctx.insertInto(table("rated_cdr"))
           .columns(field("cdr_id"), field("contract_id"), field("package_id"), field("service_type"), 
                    field("fu_consumed"), field("billable_units"), field("charged_amount"))
           .values(cdrId, contractId, packageId, serviceType, fuConsumed, billableUnits, chargedAmount)
           .execute();
    }
}
