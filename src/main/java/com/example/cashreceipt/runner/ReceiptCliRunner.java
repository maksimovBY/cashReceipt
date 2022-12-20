package com.example.cashreceipt.runner;

import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Component
public class ReceiptCliRunner implements CommandLineRunner {


    private final static String CASHIER_NAME = "Cashier 1";

    private final ReceiptService receiptService;

    @Override
    public void run(String... args) {
        String validationErrorMessage = receiptService.getValidationErrorMessage(args);
        if (hasText(validationErrorMessage)) {
            System.out.println(validationErrorMessage);
            return;
        }
        Receipt receipt = receiptService.createReceiptFromParams(CASHIER_NAME, args);
    }
}