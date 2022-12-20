package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.dto.ReceiptRow;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReceiptCliExportService {

    private static final DecimalFormat DF = new DecimalFormat("####0.00");
    private static final String HEADER = """
            \t\t\t  CASH RECEIPT
            \t\t   SUPERMARKET MAXIMUS
            \t  12, MILKY-WAY Galaxy / Earth
            \t\t\tTel: 123-456-7890
            """;

    private static final String SINGLE_SEPARATION_LINE = "---------------------------------------------";
    private static final String DOUBLE_SEPARATION_LINE = "=============================================";
    private static final String PRODUCT_HEADING_LINE = "QTY\t\tDESCRIPTION\t\t\tPRICE\t\tTOTAL";

    public String export(Receipt receipt) {
        StringBuilder builder = new StringBuilder(HEADER);
        builder.append("\n")
                .append("CASHIER: ")
                .append(receipt.getCashierName())
                .append("\t\t\t")
                .append("DATE: ")
                .append(receipt.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))
                .append("\n")
                .append("\t\t\t\t\t\t\t")
                .append("TIME: ")
                .append(receipt.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append("\n")
                .append(SINGLE_SEPARATION_LINE)
                .append("\n")
                .append(PRODUCT_HEADING_LINE)
                .append("\n\n");
        for (ReceiptRow row : receipt.getRows()) {
            builder.append(row.getQuantity())
                    .append("\t\t")
                    .append(row.getProduct().getDescription())
                    .append("\t\t\t")
                    .append(DF.format(row.getProduct().getPrice()))
                    .append("\t\t")
                    .append(DF.format(row.getRowTotalPrice()));
            if (row.getRowTotalPriceWithDiscount() != null) {
                builder.append("\n")
                        .append("\t\t\t")
                        .append(" with promotional discount: ")
                        .append(row.getRowTotalPriceWithDiscount());
            }
            builder.append("\n");
        }
        builder.append("\n")
                .append(DOUBLE_SEPARATION_LINE)
                .append("\n");
        if (receipt.getDiscountAmount() > 0) {
            builder.append("Total without discount:")
                    .append("\t\t\t\t\t")
                    .append(DF.format(receipt.getTotalPriceWithoutDiscount()))
                    .append("\n");
            builder.append("Card discount amount:")
                    .append("\t\t\t\t\t")
                    .append(DF.format(receipt.getDiscountAmount()))
                    .append("\n");
        }
        builder.append("Total:")
                .append("\t\t\t\t\t\t\t\t\t")
                .append(DF.format(receipt.getTotalPrice()))
                .append("\n");
        return builder.toString();
    }
}


