package com.billing.billreporter;

import com.telecom.billing.db.Tables;
import com.telecom.billing.db.tables.pojos.InvoiceItem;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;

public class BillRepository {

    /**
     * Fetches the invoice record matching the given invoiceId, joins it with
     * the contract, users, and rateplan tables to collect all required data,
     * then fetches all associated invoice items and constructs a Bill object.
     *
     * @param ctx       the jOOQ DSL context
     * @param invoiceId the ID of the invoice to look up
     * @return a fully populated Bill, or null if no matching invoice is found
     */
    public static Bill getInfo(DSLContext ctx, Integer invoiceId) {

        // Fetch invoice row joined with contract, users, and rateplan
        Record record = ctx
                .select()
                .from(Tables.INVOICE)
                .join(Tables.CONTRACT)
                    .on(Tables.INVOICE.CONTRACT_ID.eq(Tables.CONTRACT.CONTRACT_ID))
                .join(Tables.USERS)
                    .on(Tables.CONTRACT.USER_ID.eq(Tables.USERS.USER_ID))
                .join(Tables.RATEPLAN)
                    .on(Tables.CONTRACT.RATEPLAN_ID.eq(Tables.RATEPLAN.RATEPLAN_ID))
                .where(Tables.INVOICE.INVOICE_ID.eq(invoiceId))
                .fetchOne();

        if (record == null) {
            return null;
        }

        // Fetch all invoice items for this invoice
        List<InvoiceItem> items = ctx
                .selectFrom(Tables.INVOICE_ITEM)
                .where(Tables.INVOICE_ITEM.INVOICE_ID.eq(invoiceId))
                .fetchInto(InvoiceItem.class);

        return new Bill(
                record.get(Tables.USERS.FULL_NAME),
                record.get(Tables.USERS.EMAIL),
                record.get(Tables.CONTRACT.MSISDN),   // subscriber phone number
                record.get(Tables.INVOICE.INVOICE_ID),
                record.get(Tables.INVOICE.CYCLE_START),
                record.get(Tables.INVOICE.CYCLE_END),
                record.get(Tables.INVOICE.BILL_DATE),
                record.get(Tables.INVOICE.TOTAL_BEFORE_TAX),
                record.get(Tables.INVOICE.TAX_AMOUNT),
                record.get(Tables.INVOICE.TOTAL_AMOUNT),
                record.get(Tables.INVOICE.CURRENCY),
                record.get(Tables.RATEPLAN.NAME),
                record.get(Tables.RATEPLAN.DESCRIPTION),
                items
        );
    }
}
