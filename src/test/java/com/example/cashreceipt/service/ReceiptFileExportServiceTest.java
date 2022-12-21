package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.Receipt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Objects;

import static com.example.cashreceipt.service.Utils.getReceipt;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptFileExportServiceTest {

    ReceiptFileExportService receiptFileExportService = new ReceiptFileExportService();

    @Test
    void export(@TempDir Path tempDir) {
        Receipt check = getReceipt();
        int fileCountBeforeExport = Objects.requireNonNull(tempDir.toFile().list()).length;
        receiptFileExportService.export(check, tempDir.toString());
        int fileCountAfterExport = Objects.requireNonNull(tempDir.toFile().list()).length;

        assertEquals(0, fileCountBeforeExport);
        assertEquals(1, fileCountAfterExport);
    }

}
