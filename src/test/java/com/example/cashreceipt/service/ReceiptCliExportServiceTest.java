package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.Receipt;
import org.junit.jupiter.api.Test;

import static com.example.cashreceipt.service.Utils.getReceipt;
import static org.junit.jupiter.api.Assertions.*;

class ReceiptCliExportServiceTest {

    ReceiptCliExportService receiptCliExportService = new ReceiptCliExportService();

    @Test
    void export() {
        Receipt receipt = getReceipt();
        String exportResult = receiptCliExportService.export(receipt);
        String expected = """
			  CASH RECEIPT
		   SUPERMARKET MAXIMUS
	  12, MILKY-WAY Galaxy / Earth
			Tel: 123-456-7890

CASHIER: Cashier 1			DATE: 01/01/2020
							TIME: 10:10:00
---------------------------------------------
QTY		DESCRIPTION			PRICE		TOTAL

1		Product 1			1,20		1,20
6		Product 2			2,00		12,00
			 with promotional discount: 10,80
1		Product 3			3,33		3,33
4		Product 4			0,17		0,68

=============================================
Total without discount:					16,01
Card discount amount:					0,16
Total:									15,85
            """;
        assertEquals(expected, exportResult);
    }
}
