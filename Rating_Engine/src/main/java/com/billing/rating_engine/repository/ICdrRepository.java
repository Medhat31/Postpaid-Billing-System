package com.billing.rating_engine.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

public interface ICdrRepository {
    Result<Record> fetchUnratedCdrs(DSLContext ctx, int batchSize);
    void updateCdrAfterRating(DSLContext ctx, long cdrId, double chargedAmount);
}