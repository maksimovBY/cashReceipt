package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.DiscountCard;
import com.example.cashreceipt.dto.Product;
import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.dto.ReceiptRow;
import com.example.cashreceipt.exception.ReceiptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.stream.IntStream;

import static com.example.cashreceipt.service.ReceiptService.AVAILABLE_PRODUCTS;
import static com.example.cashreceipt.service.ReceiptService.REGISTERED_DISCOUNT_CARDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ReceiptServiceTest {

    private static final int MIN_QUANTITY_TO_APPLY_DISCOUNT = 6;
    private static final double DISCOUNT_PERCENTAGE = 10.0;
    private static final double DELTA = 0.001;

    ReceiptService receiptService = new ReceiptService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(receiptService, "minQuantityToApplyDiscount", MIN_QUANTITY_TO_APPLY_DISCOUNT);
        ReflectionTestUtils.setField(receiptService, "discountPercentage", DISCOUNT_PERCENTAGE);
    }

    @Test
    void openReceipt() {
        String cashierName = "someCashierName";
        Receipt receipt = receiptService.openReceipt(cashierName);

        assertNotNull(receipt);
        assertEquals(cashierName, receipt.getCashierName());
        assertNotNull(receipt.getDate());
        assertNotNull(receipt.getTime());
        assertNotNull(receipt.getRows());
        assertTrue(receipt.getRows().isEmpty());
    }

    @Test
    void openTwoReceipt() {
        String cashierName = "someCashierName";
        Receipt receipt1 = receiptService.openReceipt(cashierName);
        Receipt receipt2 = receiptService.openReceipt(cashierName);

        assertEquals(receipt1.getId() + 1, receipt2.getId());
    }

    @Test
    void openReceiptNotOkBlankCashierName() {
        try {
            receiptService.openReceipt(null);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ReceiptException);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void addPositionToReceipt() {
        String cashierName = "someCashierName";
        long productId = 1L;
        int productQuantity = 1;

        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, productId, productQuantity);

        assertEquals(1, receipt.getRows().size());
        assertEquals(AVAILABLE_PRODUCTS.get(productId).getPrice(), receipt.getTotalPrice(), DELTA);
        assertEquals(AVAILABLE_PRODUCTS.get(productId).getPrice(), receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals(productQuantity, receipt.getRows().stream().findFirst().map(ReceiptRow::getQuantity).orElseThrow());
    }

    @Test
    void addTwoPositionsToReceipt() {
        String cashierName = "someCashierName";
        long product1Id = 1L;
        Product product1 = AVAILABLE_PRODUCTS.get(product1Id);
        int product1Quantity = 2;
        long product2Id = 2L;
        Product product2 = AVAILABLE_PRODUCTS.get(product2Id);
        int product2Quantity = 3;

        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, product1Id, product1Quantity);
        receiptService.addPositionToReceipt(receipt, product2Id, product2Quantity);

        assertEquals(2, receipt.getRows().size());
        assertEquals(product1.getPrice() * product1Quantity + product2.getPrice() * product2Quantity,
                receipt.getTotalPrice(), DELTA);
        assertEquals(product1.getPrice() * product1Quantity + product2.getPrice() * product2Quantity,
                receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals(5, receipt.getRows().stream().map(ReceiptRow::getQuantity).reduce(Integer::sum).orElseThrow());
    }

    @Test
    void addPositionToReceiptNotOkProductNotExists() {
        String cashierName = "someCashierName";
        long productId = 999L;
        int productQuantity = 1;

        Receipt receipt = receiptService.openReceipt(cashierName);

        try {
            receiptService.addPositionToReceipt(receipt, productId, productQuantity);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ReceiptException);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void addPositionToReceiptNotOkReceiptNull() {
        long productId = 1L;
        int productQuantity = 1;

        try {
            receiptService.addPositionToReceipt(null, productId, productQuantity);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ReceiptException);
            assertNotNull(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void addPositionToReceiptNotOkIncorrectQuantity(int productQuantity) {
        String cashierName = "someCashierName";
        long productId = 1L;

        Receipt receipt = receiptService.openReceipt(cashierName);

        try {
            receiptService.addPositionToReceipt(receipt, productId, productQuantity);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ReceiptException);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void addPromotionalPositionToReceiptQuantityLessThanRequired() {
        String cashierName = "someCashierName";
        Product promotionalProduct = AVAILABLE_PRODUCTS.entrySet().stream()
                .filter(e -> e.getValue().isPromotional())
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();
        int productQuantity = MIN_QUANTITY_TO_APPLY_DISCOUNT - 1;

        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, promotionalProduct.getId(), productQuantity);

        assertEquals(1, receipt.getRows().size());
        assertEquals(promotionalProduct.getPrice() * productQuantity, receipt.getTotalPrice(), DELTA);
        assertEquals(promotionalProduct.getPrice() * productQuantity, receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals(0, receipt.getDiscountAmount());
        assertEquals(productQuantity, receipt.getRows().stream().findFirst().map(ReceiptRow::getQuantity).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(ints = {MIN_QUANTITY_TO_APPLY_DISCOUNT, MIN_QUANTITY_TO_APPLY_DISCOUNT + 1})
    void addPromotionalPositionToReceiptQuantityEnoughToApplyDiscount(int productQuantity) {

        String cashierName = "someCashierName";
        Product promotionalProduct = AVAILABLE_PRODUCTS.entrySet().stream()
                .filter(e -> e.getValue().isPromotional())
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();

        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, promotionalProduct.getId(), productQuantity);

        assertEquals(1, receipt.getRows().size());
        assertEquals(receipt.getTotalPrice(),
                promotionalProduct.getPrice() * productQuantity * (1.0 - DISCOUNT_PERCENTAGE / 100), DELTA);
    }

    @Test
    void addNotPromotionalPositionToReceiptQuantityEnoughToApplyDiscount() {
        String cashierName = "someCashierName";
        Product promotionalProduct = AVAILABLE_PRODUCTS.entrySet().stream()
                .filter(e -> !e.getValue().isPromotional())
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();

        int productQuantity = MIN_QUANTITY_TO_APPLY_DISCOUNT;
        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, promotionalProduct.getId(), productQuantity);

        assertEquals(1, receipt.getRows().size());
        assertEquals(receipt.getTotalPrice(), promotionalProduct.getPrice() * productQuantity, DELTA);
    }

    @ParameterizedTest
    @ValueSource(ints = {MIN_QUANTITY_TO_APPLY_DISCOUNT, MIN_QUANTITY_TO_APPLY_DISCOUNT + 1})
    void addPromotionalMultiPositionToReceiptQuantityEnoughToApplyDiscount(int productQuantity) {

        String cashierName = "someCashierName";
        Product promotionalProduct = AVAILABLE_PRODUCTS.entrySet().stream()
                .filter(e -> e.getValue().isPromotional())
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();

        Receipt receipt = receiptService.openReceipt(cashierName);
        IntStream.range(0, productQuantity)
                .forEach(i -> receiptService.addPositionToReceipt(receipt, promotionalProduct.getId(), 1));

        assertEquals(productQuantity, receipt.getRows().size());
        assertEquals(receipt.getTotalPrice(),
                promotionalProduct.getPrice() * productQuantity * (1.0 - DISCOUNT_PERCENTAGE / 100), DELTA);
    }

    @Test
    void applyDiscountCardToReceipt() {
        String cashierName = "someCashierName";
        long p1Id = 1L;
        Product p1 = AVAILABLE_PRODUCTS.get(p1Id);
        int p1Qnt = 2;
        long p2Id = 2L;
        Product p2 = AVAILABLE_PRODUCTS.get(p2Id);
        int p2Qnt = 3;
        DiscountCard card = REGISTERED_DISCOUNT_CARDS.entrySet().stream()
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();

        Receipt receipt = receiptService.openReceipt(cashierName);
        receiptService.addPositionToReceipt(receipt, p1Id, p1Qnt);
        receiptService.addPositionToReceipt(receipt, p2Id, p2Qnt);

        receiptService.applyDiscountCardToReceipt(receipt, card.getId());

        assertEquals(2, receipt.getRows().size());
        assertEquals(p1.getPrice() * p1Qnt + p2.getPrice() * p2Qnt, receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals((p1.getPrice() * p1Qnt + p2.getPrice() * p2Qnt) * (1.0 - card.getDiscountPercentage() / 100.0),
                receipt.getTotalPrice(), DELTA);
        assertEquals(receipt.getTotalPrice() + receipt.getDiscountAmount(), receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals(5, receipt.getRows().stream().map(ReceiptRow::getQuantity).reduce(Integer::sum).orElseThrow());
    }

    @Test
    void validReceiptParams() {
        String[] args = {" 1-1", "2-2 ", "3-3"};
        String validationErrorMessage = receiptService.getValidationErrorMessage(args);
        assertTrue(validationErrorMessage.isEmpty());
    }

    @Test
    void invalidReceiptProductsQuantityParamsExtraSpace() {
        String[] args = {" 1-1", "2-2 ", "3 -3"};
        String validationErrorMessage = receiptService.getValidationErrorMessage(args);
        assertFalse(validationErrorMessage.isEmpty());
    }

    @Test
    void invalidReceiptProductsQuantityParamsExtraHyphen() {
        String[] args = {" 1-1", "2-2 ", "3-3-3"};
        String validationErrorMessage = receiptService.getValidationErrorMessage(args);
        assertFalse(validationErrorMessage.isEmpty());
    }

    @Test
    void createReceiptFromParamsOk(){
        String cashierName = "someCashierName";
        long p1Id = 1L;
        Product p1 = AVAILABLE_PRODUCTS.get(p1Id);
        int p1Qnt = 2;
        long p2Id = 2L;
        Product p2 = AVAILABLE_PRODUCTS.get(p2Id);
        int p2Qnt = 3;
        DiscountCard card = REGISTERED_DISCOUNT_CARDS.entrySet().stream()
                .findAny()
                .map(Map.Entry::getValue)
                .orElseThrow();

        String[] args = {p1Id + "-" + p1Qnt, "card-" + card.getId(), p2Id + "-" + p2Qnt};

        Receipt receipt = receiptService.createReceiptFromParams(cashierName, args);

        assertEquals(cashierName, receipt.getCashierName());
        assertEquals(2, receipt.getRows().size());
        assertEquals(p1.getPrice() * p1Qnt + p2.getPrice() * p2Qnt, receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals((p1.getPrice() * p1Qnt + p2.getPrice() * p2Qnt) * (1.0 - card.getDiscountPercentage() / 100.0),
                receipt.getTotalPrice(), DELTA);
        assertEquals(receipt.getTotalPrice() + receipt.getDiscountAmount(), receipt.getTotalPriceWithoutDiscount(), DELTA);
        assertEquals(5, receipt.getRows().stream().map(ReceiptRow::getQuantity).reduce(Integer::sum).orElseThrow());

    }
}