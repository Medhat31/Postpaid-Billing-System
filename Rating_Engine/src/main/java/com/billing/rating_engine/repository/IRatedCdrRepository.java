package com.billing.rating_engine.repository;

import org.jooq.DSLContext;

/**
 *
 * @author mfathy
 */


public interface IRatedCdrRepository {
    void insertRatedRecord(DSLContext ctx, long cdrId, int contractId, int packageId, 
                           short serviceType, double fuConsumed, double billableUnits, double chargedAmount);
}
