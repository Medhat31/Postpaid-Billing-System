package com.billing.aggregator;

import com.telecom.billing.db.Tables;
import com.telecom.billing.db.enums.InvoiceItemType; 
import com.telecom.billing.db.tables.pojos.InvoiceItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

/**
 * Repository responsible for executing the monthly billing aggregation.
 * * This class handles the financial logic of gathering base subscription fees 
 * and aggregating out-of-bundle CDR usage. It ensures data integrity by 
 * wrapping the entire calculation, invoice generation, and audit locking 
 * (marking CDRs as billed and resetting unbilled amounts) within a strict 
 * database transaction.
 */
public class AggregationRepository {

    /**
     * Aggregates usage and creates a unified invoice for a specific contract.
     *
     * @param parentCtx  The active jOOQ DSLContext
     * @param contractId The ID of the contract to bill
     * @param cycleStart The start date of the billing cycle
     * @param cycleEnd   The end date of the billing cycle
     * @param billDate   The date the bill is generated
     * @return true if the invoice was successfully created, false if the contract was not found
     */
    public static boolean aggregateAndCreateInvoice(
            DSLContext parentCtx, 
            Integer contractId, 
            LocalDate cycleStart, 
            LocalDate cycleEnd, 
            LocalDate billDate) {

        return parentCtx.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            
            List<InvoiceItem> itemsList = new ArrayList<>();
            BigDecimal totalBeforeTax = BigDecimal.ZERO;
            BigDecimal totalOutOfBundle = BigDecimal.ZERO;

            // =====================================================================
            // CONTRACT & BASE PLAN VERIFICATION
            // =====================================================================
            
            Record contractRecord = ctx
                    .select()
                    .from(Tables.CONTRACT)
                    .join(Tables.RATEPLAN).on(Tables.CONTRACT.RATEPLAN_ID.eq(Tables.RATEPLAN.RATEPLAN_ID))
                    .where(Tables.CONTRACT.CONTRACT_ID.eq(contractId))
                    .fetchOne();

            if (contractRecord == null) {
                return false; 
            }

            BigDecimal baseFee = contractRecord.get(Tables.RATEPLAN.MONTHLY_FEE);
            if (baseFee == null) baseFee = BigDecimal.ZERO;
            
            totalBeforeTax = totalBeforeTax.add(baseFee);

            InvoiceItem baseItem = new InvoiceItem();
            baseItem.setDescription("Base Subscription: " + contractRecord.get(Tables.RATEPLAN.NAME));
            baseItem.setAmount(baseFee);
            baseItem.setItemType(InvoiceItemType.subscription); 
            itemsList.add(baseItem);

            // =====================================================================
            // THE CDR AGGREGATION ENGINE
            // =====================================================================
            
            Result<?> cdrAggregations = ctx
                    .select(Tables.CDR.SERVICE_TYPE, DSL.sum(Tables.CDR.CHARGED_AMOUNT).as("total_extra"))
                    .from(Tables.CDR)
                    .where(Tables.CDR.CONTRACT_ID.eq(contractId))
                    .and(Tables.CDR.START_TIME.between(cycleStart.atStartOfDay(), cycleEnd.atTime(23, 59, 59)))
                    .and(Tables.CDR.IS_RATED.eq(true))
                    .and(Tables.CDR.IS_BILLED.eq(false))
                    .and(Tables.CDR.CHARGED_AMOUNT.gt(BigDecimal.ZERO))
                    .groupBy(Tables.CDR.SERVICE_TYPE)
                    .fetch();

            for (Record cdrRecord : cdrAggregations) {
                String serviceType = cdrRecord.get(Tables.CDR.SERVICE_TYPE, String.class);
                BigDecimal extraCharge = cdrRecord.get("total_extra", BigDecimal.class);
                
                totalBeforeTax = totalBeforeTax.add(extraCharge);
                totalOutOfBundle = totalOutOfBundle.add(extraCharge);

                InvoiceItem extraItem = new InvoiceItem();
                extraItem.setDescription("Out of Bundle Usage: " + serviceType);
                extraItem.setAmount(extraCharge);
                extraItem.setItemType(InvoiceItemType.usage);
                itemsList.add(extraItem);
            }
            
            // =====================================================================
            // TOTAL AFTER TAX CALCULATION & INVOICE CREATION
            // =====================================================================

            BigDecimal taxAmount = totalBeforeTax.multiply(new BigDecimal("0.14")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = totalBeforeTax.add(taxAmount);

            Integer newInvoiceId = ctx.insertInto(Tables.INVOICE)
                    .set(Tables.INVOICE.CONTRACT_ID, contractId)
                    .set(Tables.INVOICE.CYCLE_START, cycleStart)
                    .set(Tables.INVOICE.CYCLE_END, cycleEnd)
                    .set(Tables.INVOICE.BILL_DATE, billDate)
                    .set(Tables.INVOICE.TOTAL_BEFORE_TAX, totalBeforeTax)
                    .set(Tables.INVOICE.TAX_AMOUNT, taxAmount)
                    .set(Tables.INVOICE.TOTAL_AMOUNT, totalAmount)
                    .set(Tables.INVOICE.CURRENCY, "EGP")
                    .returning(Tables.INVOICE.INVOICE_ID)
                    .fetchOne()
                    .getInvoiceId();
            
            // =====================================================================
            // DATA INSERTION in DB & MARKING CDR AS BILLED 
            // =====================================================================

            for (InvoiceItem item : itemsList) {
                ctx.insertInto(Tables.INVOICE_ITEM)
                        .set(Tables.INVOICE_ITEM.INVOICE_ID, newInvoiceId)
                        .set(Tables.INVOICE_ITEM.DESCRIPTION, item.getDescription())
                        .set(Tables.INVOICE_ITEM.AMOUNT, item.getAmount())
                        .set(Tables.INVOICE_ITEM.ITEM_TYPE, item.getItemType())
                        .execute();
            }

            ctx.update(Tables.CDR)
                    .set(Tables.CDR.IS_BILLED, true)
                    .where(Tables.CDR.CONTRACT_ID.eq(contractId))
                    .and(Tables.CDR.START_TIME.between(cycleStart.atStartOfDay(), cycleEnd.atTime(23, 59, 59)))
                    .and(Tables.CDR.IS_RATED.eq(true))
                    .and(Tables.CDR.IS_BILLED.eq(false)) 
                    .execute();

            ctx.update(Tables.CONTRACT)
                    .set(Tables.CONTRACT.UNBILLED_AMOUNT, Tables.CONTRACT.UNBILLED_AMOUNT.minus(totalOutOfBundle))
                    .where(Tables.CONTRACT.CONTRACT_ID.eq(contractId))
                    .execute();

            return true; 
        }); 
    }
}