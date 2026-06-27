package com.billing.aggregator;

import com.telecom.billing.db.Tables;
import java.time.LocalDate;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record1;

public class Aggregator {

    public static void main(String[] args) {
        try {
            DSLContext mainCtx = DatabaseAdaptor.connect();
            
            System.out.println("=========================================");
            System.out.println("Starting Aggregation Sweep & Invoice Generation");
            System.out.println("=========================================");

            LocalDate billDate = LocalDate.now(); 
            
            LocalDate cycleStart = billDate.withDayOfMonth(1);
            LocalDate cycleEnd = cycleStart.withDayOfMonth(cycleStart.lengthOfMonth());

            System.out.println("Sweeping all unbilled CDRs...");
            System.out.println("Invoices will be labeled for cycle: " + cycleStart + " to " + cycleEnd);
            
            int count = 0;

            // Get active contracts
            try (Cursor<Record1<Integer>> cursor = mainCtx
                    .select(Tables.CONTRACT.CONTRACT_ID)
                    .from(Tables.CONTRACT)
                    .where(Tables.CONTRACT.STATUS.eq("active")) 
                    .fetchLazy()) {

                // Process each contract
                for (Record1<Integer> record : cursor) {
                    Integer contractId = record.value1();
                    
                    try {
                        // Pass the presentation dates to the repository
                        boolean success = AggregationRepository.aggregateAndCreateInvoice(
                                mainCtx, contractId, cycleStart, cycleEnd, billDate
                        );

                        if (success) {
                            count++;
                            System.out.println("Swept and billed Contract: " + contractId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error : Contract " + contractId + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("=========================================");
            System.out.println("Sweep Complete! Generated " + count + " invoices.");
            System.out.println("=========================================");

        } catch (Exception e) {
            System.err.println("Fatal error during execution:");
            e.printStackTrace();
        }
    }
}