package com.billing.billreporter;

import com.telecom.billing.db.tables.pojos.InvoiceItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Bill {

    // User info
    private final String userName;
    private final String userEmail;
    private final String userPhone;

    // Invoice info
    private final Integer invoiceId;
    private final LocalDate cycleStart;
    private final LocalDate cycleEnd;
    private final LocalDate billDate;
    private final BigDecimal totalBeforeTax;
    private final BigDecimal taxAmount;
    private final BigDecimal totalAmount;
    private final String currency;

    // Rateplan info
    private final String rateplanName;
    private final String rateplanDescription;

    // Invoice items
    private final List<InvoiceItem> items;

    public Bill(
            String userName,
            String userEmail,
            String userPhone,
            Integer invoiceId,
            LocalDate cycleStart,
            LocalDate cycleEnd,
            LocalDate billDate,
            BigDecimal totalBeforeTax,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            String currency,
            String rateplanName,
            String rateplanDescription,
            List<InvoiceItem> items) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.invoiceId = invoiceId;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        this.billDate = billDate;
        this.totalBeforeTax = totalBeforeTax;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.rateplanName = rateplanName;
        this.rateplanDescription = rateplanDescription;
        this.items = items;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public LocalDate getCycleStart() {
        return cycleStart;
    }

    public LocalDate getCycleEnd() {
        return cycleEnd;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public BigDecimal getTotalBeforeTax() {
        return totalBeforeTax;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getRateplanName() {
        return rateplanName;
    }

    public String getRateplanDescription() {
        return rateplanDescription;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }
}
