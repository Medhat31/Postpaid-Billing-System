package com.billing.rating_engine.processor;

import org.jooq.DSLContext;
import org.jooq.Record;

/**
 *
 * @author mfathy
 */
public interface ICdrProcessor {
    void process(DSLContext ctx, Record cdr, Record pricingMatrix, Record wallet);
}
