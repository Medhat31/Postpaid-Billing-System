package com.billing.cdrparser;

import com.telecom.billing.db.tables.*;
import java.util.List;
import org.jooq.DSLContext;

/**
 * Handles database operations for writing call detail records.
 * This class translates structured {@link LineItem} records into SQL inserts
 * executed via jOOQ mapping.
 *
 * @author seifabsalam
 * @version 1.0
 * @since 1.0
 */
public class DatabaseWriter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseWriter() {
        // Utility class has only static methods and should not be instantiated.
    }

    /**
     * Static reference to the jOOQ {@link Cdr} database table definition.
     */
    private static final Cdr CDR = Cdr.CDR;

    /**
     * Inserts a list of line items sequentially into the database.
     * Maps each {@code LineItem} property to its corresponding table column in the CDR schema.
     *
     * @param items the list of parsed call detail records to write
     * @param ctx the active database execution context
     */
    public static void write(List<LineItem> items, DSLContext ctx) {
        for (LineItem item : items) {
            ctx.insertInto(CDR)
                    .set(CDR.CONTRACT_ID, item.getContractId())
                    .set(CDR.DIAL_A, item.getDialA())
                    .set(CDR.DIAL_B, item.getDialB())
                    .set(CDR.DURATION, item.getDuration())
                    .set(CDR.EXTERNAL_FEE_PIASTERS, item.getExternalFeePiasters())
                    .set(CDR.SERVICE_TYPE, item.getServiceType())
                    .set(CDR.START_TIME, item.getStartTime())
                    .set(CDR.IS_RATED, item.isRated())
                    .execute();
        }
    }
}
