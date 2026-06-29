package com.billing.rating_engine.repository;

import java.math.BigDecimal;
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
    public void updateCdrAfterRating(DSLContext ctx, long cdrId, double chargedAmount) {
        ctx.update(table("cdr"))
                .set(field("is_rated"), true)
                .set(field("charged_amount"), chargedAmount)
                .where(field("cdr_id").eq(cdrId))
                .execute();
    }

}
