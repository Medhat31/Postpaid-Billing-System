package com.billing.rating_engine.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import static org.jooq.impl.DSL.*;

public class CdrRepository implements ICdrRepository {
    @Override
    public Result<Record> fetchUnratedCdrs(DSLContext ctx, int batchSize) {
        return ctx.select()
                .from(table("cdr"))
                .where(field("is_rated").eq(false))
                .limit(batchSize)
                .fetch();
    }

    @Override
    public void markAsRated(DSLContext ctx, long cdrId) {
        ctx.update(table("cdr"))
           .set(field("is_rated"), true)
           .where(field("cdr_id").eq(cdrId))
           .execute();
    }
}