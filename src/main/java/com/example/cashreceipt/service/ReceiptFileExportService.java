package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.dto.ReceiptRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ReceiptFileExportService {

    private static final DecimalFormat DF = new DecimalFormat("####0.00");
    private static final String HEADER = """
            \t\t\t\tCASH RECEIPT
            \t\t\tSUPERMARKET MAXIMUS
            \t\t12, MILKY-WAY Galaxy / Earth
            \t\t\t  Tel :123-456-7890
            """;
    private static final String SINGLE_SEPARATION_LINE = "---------------------------------------------";
    private static final String DOUBLE_SEPARATION_LINE = "=============================================";
    private static final String PRODUCT_HEADING_LINE = "QTY\t\tDESCRIPTION\t\t\tPRICE\t\tTOTAL";

    public void export(Receipt receipt, String pathToOutputDirectory) {
        String dateFormatted = receipt.getDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        String timeFormatted = receipt.getTime().format(DateTimeFormatter.ofPattern("HH-mm-ss"));
        String pathToFile = pathToOutputDirectory +
                (pathToOutputDirectory.endsWith(System.getProperty("file.separator"))
                        ? ""
                        : System.getProperty("file.separator")) +
                dateFormatted +
                "-" +
                timeFormatted +
                ".txt";
        String value = exportToFileString(receipt);
        try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pathToFile)))) {
            os.write(value.getBytes());
        } catch (IOException e) {
            log.error("Failed export to file", e);
        }
    }

    private String exportToFileString(Receipt receipt) {
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
                        .append("with promotional discount:\t")
                        .append(DF.format(row.getRowTotalPriceWithDiscount()));
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