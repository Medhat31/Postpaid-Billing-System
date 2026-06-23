package com.billing.rating_engine.engine;

import org.jooq.DSLContext;

/**
 *
 * @author mfathy
 */
public interface IRatingEngine {
    void processNextBatch(DSLContext mainCtx);
}
