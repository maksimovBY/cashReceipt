package com.example.cashreceipt.runner;

import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.service.ReceiptCliExportService;
import com.example.cashreceipt.service.ReceiptFileExportService;
import com.example.cashreceipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Component
public class ReceiptCliRunner implements CommandLineRunner {

    @Value("${export.destination-folder-path}")
    private String destinationFolderPath;

    private final static String CASHIER_NAME = "Cashier 1";

    private final ReceiptService receiptService;
    private final ReceiptCliExportService receiptCliExportService;
    private final ReceiptFileExportService receiptFileExportService;

    @Override
    public void run(String... args) {
        String validationErrorMessage = receiptService.getValidationErrorMessage(args);
        if (hasText(validationErrorMessage)) {
            System.out.println(validationErrorMessage);
            return;
        }
        Receipt receipt = receiptService.createReceiptFromParams(CASHIER_NAME, args);
        String formattedReceipt = receiptCliExportService.export(receipt);
        receiptFileExportService.export(receipt, destinationFolderPath);
        System.out.println(formattedReceipt);
    }
}