package com.billing.billreporter;

import com.telecom.billing.db.Tables;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;

public class BillReporter {

    public static void main(String[] args) {
        try {
            // 1. Initialise the database connection
            DSLContext mainCtx = DatabaseAdaptor.connect();

            // 2. Fetch using a lazy cursor to avoid loading all invoices into memory
            System.out.println("Starting bill generation...");
            int count = 0;
            
            try (Cursor<Record1<Integer>> cursor = mainCtx
                    .select(Tables.INVOICE.INVOICE_ID)
                    .from(Tables.INVOICE)
                    .fetchLazy()) {
                
                for (Record1<Integer> record : cursor) {
                    Integer invoiceId = record.value1();
                    
                    // 3. Create a distinct DSLContext for this bill (sharing the same configuration/pool)
                    DSLContext billCtx = DSL.using(mainCtx.configuration());
                    
                    // 4. Fetch the data and generate the bill
                    Bill bill = BillRepository.getInfo(billCtx, invoiceId);
                    if (bill != null) {
                        try {
                            BillGenerator.bill(bill);
                            count++;
                        } catch (Exception e) {
                            System.err.println("Failed to generate PDF for invoice " + invoiceId + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("No matching data found for invoice " + invoiceId);
                    }
                }
            }
            
            System.out.println("Finished generating " + count + " bills.");
            
        } catch (Exception e) {
            System.err.println("Fatal error during execution:");
            e.printStackTrace();
        }
    }
}
