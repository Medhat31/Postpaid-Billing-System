package com.billing.billreporter;

import com.telecom.billing.db.tables.pojos.InvoiceItem;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import org.apache.commons.lang3.StringUtils;

public class BillGenerator {

    // ── Colour palette ──────────────────────────────────────────────────────
    private static final Color PRIMARY      = new Color(0x1A, 0x37, 0x6C); // deep navy
    private static final Color ACCENT       = new Color(0x2E, 0x86, 0xC1); // steel blue
    private static final Color LIGHT_BG     = new Color(0xF0, 0xF4, 0xF8); // very light grey-blue
    private static final Color TEXT_DARK    = new Color(0x1A, 0x1A, 0x2E); // near-black
    private static final Color TEXT_MUTED   = new Color(0x55, 0x66, 0x77); // muted slate
    private static final Color WHITE        = Color.WHITE;
    private static final Color DIVIDER      = new Color(0xCC, 0xD6, 0xE0); // soft border

    /**
     * Generates a professionally styled PDF invoice for the given Bill and
     * writes it to the directory defined by the {@code output.dir} key in
     * {@code io.properties}.
     *
     * @param bill the fully populated Bill object to render
     * @throws Exception if the report cannot be built or written
     */
    public static void bill(Bill bill) throws Exception {

        // ── 1. Load output directory ─────────────────────────────────────────
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("io.properties")) {
            props.load(in);
        }
        String outputDirStr = props.getProperty("output.dir", "output");
        File outDir = new File(outputDirStr);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        String pdfPath = outputDirStr + File.separator + "Invoice_" + bill.getInvoiceId() + ".pdf";

        // ── 2. Define styles ─────────────────────────────────────────────────

        // Large, bold, white title on navy background
        StyleBuilder titleStyle = stl.style()
                .setFont(stl.font().setFontSize(22).bold())
                .setForegroundColor(WHITE)
                .setBackgroundColor(PRIMARY)
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                .setPadding(14);

        // Subtitle / company tag-line under the title
        StyleBuilder subtitleStyle = stl.style()
                .setFont(stl.font().setFontSize(10).italic())
                .setForegroundColor(WHITE)
                .setBackgroundColor(PRIMARY)
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setPadding(stl.padding().setTop(0).setBottom(10).setLeft(14).setRight(14));

        // Section header (e.g. "CUSTOMER DETAILS")
        StyleBuilder sectionLabelStyle = stl.style()
                .setFont(stl.font().setFontSize(9).bold())
                .setForegroundColor(WHITE)
                .setBackgroundColor(ACCENT)
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                .setPadding(stl.padding(6));

        // Label in a key-value pair row
        StyleBuilder labelStyle = stl.style()
                .setFont(stl.font().setFontSize(9).bold())
                .setForegroundColor(TEXT_MUTED)
                .setBackgroundColor(LIGHT_BG)
                .setPadding(stl.padding().setTop(5).setBottom(5).setLeft(8).setRight(4));

        // Value in a key-value pair row
        StyleBuilder valueStyle = stl.style()
                .setFont(stl.font().setFontSize(9))
                .setForegroundColor(TEXT_DARK)
                .setBackgroundColor(WHITE)
                .setPadding(stl.padding().setTop(5).setBottom(5).setLeft(6).setRight(8));

        // Column header in the items table
        StyleBuilder colHeaderStyle = stl.style()
                .setFont(stl.font().setFontSize(9).bold())
                .setForegroundColor(WHITE)
                .setBackgroundColor(PRIMARY)
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                .setPadding(stl.padding(7))
                .setBorder(stl.pen1Point().setLineColor(PRIMARY));

        // Regular row cell in the items table
        StyleBuilder rowStyle = stl.style()
                .setFont(stl.font().setFontSize(9))
                .setForegroundColor(TEXT_DARK)
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                .setPadding(stl.padding(6))
                .setBorder(stl.pen(0.5f, net.sf.dynamicreports.report.constant.LineStyle.SOLID)
                        .setLineColor(DIVIDER));

        // Alternating row (even rows)
        StyleBuilder rowStyleAlt = stl.style(rowStyle)
                .setBackgroundColor(LIGHT_BG);

        // Totals label (right-aligned, bold)
        StyleBuilder totalsLabelStyle = stl.style()
                .setFont(stl.font().setFontSize(9).bold())
                .setForegroundColor(TEXT_MUTED)
                .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                .setPadding(stl.padding().setTop(4).setBottom(4).setRight(6));

        // Totals value (right-aligned)
        StyleBuilder totalsValueStyle = stl.style()
                .setFont(stl.font().setFontSize(9))
                .setForegroundColor(TEXT_DARK)
                .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                .setPadding(stl.padding().setTop(4).setBottom(4).setRight(8));

        // Grand total row – larger, navy
        StyleBuilder grandTotalLabelStyle = stl.style(totalsLabelStyle)
                .setFont(stl.font().setFontSize(11).bold())
                .setForegroundColor(PRIMARY);

        StyleBuilder grandTotalValueStyle = stl.style(totalsValueStyle)
                .setFont(stl.font().setFontSize(11).bold())
                .setForegroundColor(PRIMARY);

        // ── 3. Build invoice items data source ───────────────────────────────
        DRDataSource ds = new DRDataSource("description", "amount");
        for (InvoiceItem item : bill.getItems()) {
            ds.add(
                    item.getDescription() != null ? item.getDescription() : "",
                    item.getAmount()
            );
        }

        // ── 4. Define columns for the items table ────────────────────────────
        TextColumnBuilder<String> descCol = col.column("Description", "description", type.stringType())
                .setStyle(rowStyle)
                .setTitleStyle(colHeaderStyle)
                .setWidth(3);   // relative width — description gets more space

        TextColumnBuilder<java.math.BigDecimal> amtCol = col.column("Amount (" + bill.getCurrency() + ")", "amount", type.bigDecimalType())
                .setStyle(stl.style(rowStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                .setTitleStyle(stl.style(colHeaderStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                .setWidth(1);

        // ── 5. Helper: build a labelled key-value row ─────────────────────────
        // Returns a HorizontalListBuilder with [label | value] in a 1:2 ratio
        java.util.function.BiFunction<String, String, net.sf.dynamicreports.report.builder.component.HorizontalListBuilder> kvRow =
                (label, value) -> cmp.horizontalList(
                        cmp.text(label).setStyle(labelStyle).setFixedWidth(140),
                        cmp.text(value != null ? value : "—").setStyle(valueStyle)
                );

        // ── 6. Assemble the main report ──────────────────────────────────────
        JasperReportBuilder report = report()
                // Page setup
                .setPageFormat(net.sf.dynamicreports.report.constant.PageType.A4)
                .setPageMargin(margin(30))

                // Title band — company header
                .title(
                        cmp.text("TELECOM BILLING STATEMENT").setStyle(titleStyle).setFixedHeight(48),
                        cmp.text("Your monthly invoice summary").setStyle(subtitleStyle).setFixedHeight(24),

                        cmp.verticalGap(16),

                        // ── Customer & Invoice details side by side ──────────
                        cmp.text("  CUSTOMER DETAILS").setStyle(sectionLabelStyle).setFixedHeight(24),
                        kvRow.apply("Name",           bill.getUserName()),
                        kvRow.apply("Email",          bill.getUserEmail()),
                        kvRow.apply("Phone / MSISDN", bill.getUserPhone()),

                        cmp.verticalGap(12),

                        cmp.text("  INVOICE DETAILS").setStyle(sectionLabelStyle).setFixedHeight(24),
                        kvRow.apply("Invoice ID",   String.valueOf(bill.getInvoiceId())),
                        kvRow.apply("Bill Date",    bill.getBillDate()    != null ? bill.getBillDate().toString()    : "—"),
                        kvRow.apply("Cycle Start",  bill.getCycleStart()  != null ? bill.getCycleStart().toString()  : "—"),
                        kvRow.apply("Cycle End",    bill.getCycleEnd()    != null ? bill.getCycleEnd().toString()    : "—"),

                        cmp.verticalGap(12),

                        cmp.text("  RATE PLAN").setStyle(sectionLabelStyle).setFixedHeight(24),
                        kvRow.apply("Plan Name",    bill.getRateplanName()),
                        kvRow.apply("Description",  bill.getRateplanDescription()),

                        cmp.verticalGap(20),

                        cmp.text("  INVOICE ITEMS").setStyle(sectionLabelStyle).setFixedHeight(24)
                )

                // Items table columns
                .columns(descCol, amtCol)
                .setColumnTitleStyle(colHeaderStyle)
                .highlightDetailEvenRows()      // built-in alternating row highlight
                .setDetailStyle(rowStyle)

                // Totals summary at the bottom
                .summary(
                        cmp.verticalGap(16),
                        cmp.horizontalList(
                                cmp.filler(),   // push totals to right automatically
                                cmp.verticalList(
                                        cmp.horizontalList(
                                                cmp.text("Subtotal:").setStyle(totalsLabelStyle).setFixedWidth(100),
                                                cmp.text(formatAmount(bill.getTotalBeforeTax(), bill.getCurrency())).setStyle(totalsValueStyle).setFixedWidth(100)
                                        ),
                                        cmp.horizontalList(
                                                cmp.text("Tax:").setStyle(totalsLabelStyle).setFixedWidth(100),
                                                cmp.text(formatAmount(bill.getTaxAmount(), bill.getCurrency())).setStyle(totalsValueStyle).setFixedWidth(100)
                                        ),
                                        cmp.line().setPen(stl.pen2Point().setLineColor(PRIMARY)),
                                        cmp.horizontalList(
                                                cmp.text("TOTAL DUE:").setStyle(grandTotalLabelStyle).setFixedWidth(100),
                                                cmp.text(formatAmount(bill.getTotalAmount(), bill.getCurrency())).setStyle(grandTotalValueStyle).setFixedWidth(100)
                                        )
                                )
                        ),
                        cmp.verticalGap(30),
                        cmp.text("Thank you for being a valued customer.")
                                .setStyle(stl.style()
                                        .setFont(stl.font().setFontSize(9).italic())
                                        .setForegroundColor(TEXT_MUTED)
                                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                )

                // Page footer with page number
                .pageFooter(
                        cmp.pageXofY()
                                .setStyle(stl.style()
                                        .setFont(stl.font().setFontSize(8))
                                        .setForegroundColor(TEXT_MUTED)
                                        .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                )

                // Attach the invoice items data source
                .setDataSource(ds);

        // ── 7. Export to PDF ─────────────────────────────────────────────────
        report.toPdf(new FileOutputStream(pdfPath));
        System.out.println("Invoice PDF written to: " + pdfPath);
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private static String formatAmount(java.math.BigDecimal amount, String currency) {
        if (amount == null) return "—";
        return String.format("%,.2f %s", amount, currency != null ? currency : "");
    }
}
