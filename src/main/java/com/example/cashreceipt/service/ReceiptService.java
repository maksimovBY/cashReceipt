package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.DiscountCard;
import com.example.cashreceipt.dto.Product;
import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.dto.ReceiptRow;
import com.example.cashreceipt.exception.ReceiptException;
import com.example.cashreceipt.mapper.DiscountCardMapper;
import com.example.cashreceipt.mapper.ProductMapper;
import com.example.cashreceipt.reposotory.DiscountCardRepository;
import com.example.cashreceipt.reposotory.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Service
public class ReceiptService {

    @Value("${promotional.min-quantity-to-apply-discount}")
    private int minQuantityToApplyDiscount;

    @Value("${promotional.discount-percentage}")
    private double discountPercentage;

    private static final AtomicLong NEXT_RECEIPT_ID = new AtomicLong(1);

    public static final String DISCOUNT_CARD_KEYWORD_PREFIX = "card-";

    public static final Map<Long, Product> AVAILABLE_PRODUCTS = Map.of(
            1L, Product.builder().id(1).description("Product 1").price(1.2).promotional(false).build(),
            2L, Product.builder().id(2).description("Product 2").price(2).promotional(true).build(),
            3L, Product.builder().id(3).description("Product 3").price(3.33).promotional(false).build(),
            4L, Product.builder().id(4).description("Product 4").price(0.17).promotional(true).build(),
            5L, Product.builder().id(5).description("Product 5").price(35.99).promotional(false).build()
    );

    public static final Map<Long, DiscountCard> REGISTERED_DISCOUNT_CARDS = Map.of(
            1L, DiscountCard.builder().id(1).customerName("Customer name 1").discountPercentage(1).build(),
            2L, DiscountCard.builder().id(2).customerName("Customer name 2").discountPercentage(2.5).build(),
            3L, DiscountCard.builder().id(3).customerName("Customer name 3").discountPercentage(3).build(),
            4L, DiscountCard.builder().id(4).customerName("Customer name 4").discountPercentage(3.5).build(),
            5L, DiscountCard.builder().id(5).customerName("Customer name 5").discountPercentage(5).build()
    );

    private final ProductRepository productRepository;
    private final DiscountCardRepository discountCardRepository;
    private final ProductMapper productMapper;
    private final DiscountCardMapper discountCardMapper;

    public Receipt openReceipt(String cashierName) {
        if (!hasText(cashierName)) {
            throw new ReceiptException("Cashier name can't be blank.");
        }

        return Receipt.builder()
                .id(NEXT_RECEIPT_ID.getAndIncrement())
                .cashierName(cashierName)
                .date(LocalDate.now())
                .time(LocalTime.now())
                .rows(new ArrayList<>())
                .build();
    }

    public void addPositionToReceipt(Receipt receipt, long productId, int productQuantity) {
        if (receipt == null) {
            throw new ReceiptException("Receipt can't be null");
        }

        if (!productRepository.existsById(productId)) {
            throw new ReceiptException("Product with id: %s is not available".formatted(productId));
        }

        if (productQuantity <= 0) {
            throw new ReceiptException("Product quantity in position must be greater then 0");
        }

        Product product = productMapper.toDto(productRepository.findById(productId).orElse(null));

        ReceiptRow receiptRow = ReceiptRow.builder()
                .product(product)
                .quantity(productQuantity)
                .rowTotalPrice(product.getPrice() * productQuantity)
                .build();

        if (receipt.getRows() == null) {
            receipt.setRows(new ArrayList<>());
        }

        receipt.getRows().add(receiptRow);

        recalculateReceiptPrice(receipt);
    }

    public void applyDiscountCardToReceipt(Receipt receipt, long cardId) {
        if (receipt == null) {
            throw new ReceiptException("Check can't be null");
        }

        if (!discountCardRepository.existsById(cardId)) {
            throw new ReceiptException("Card with id: %s is not registered".formatted(cardId));
        }

        receipt.setDiscountCard(discountCardMapper.toDto(discountCardRepository.findById(cardId).orElse(null)));

        recalculateReceiptPrice(receipt);
    }

    private void recalculateReceiptPrice(Receipt receipt) {
        applyPromotional(receipt);

        double receiptTotalPriceWithoutDiscount = receipt.getRows().stream()
                .map(receiptRow -> {
                    if (receiptRow.getRowTotalPriceWithDiscount() != null) {
                        return receiptRow.getRowTotalPriceWithDiscount();
                    }
                    return receiptRow.getRowTotalPrice();
                })
                .reduce(0.0, Double::sum);
        receipt.setTotalPriceWithoutDiscount(receiptTotalPriceWithoutDiscount);

        DiscountCard discountCard = receipt.getDiscountCard();
        if (discountCard != null) {
            double discountAmount = receiptTotalPriceWithoutDiscount * discountCard.getDiscountPercentage() / 100.0;
            receipt.setDiscountAmount(discountAmount);
        }

        receipt.setTotalPrice(receipt.getTotalPriceWithoutDiscount() - receipt.getDiscountAmount());
    }

    private void applyPromotional(Receipt receipt) {
        Map<Product, Integer> promotionalProductsQuantityMap = receipt.getRows().stream()
                .filter(r -> r.getProduct().isPromotional())
                .collect(Collectors.toMap(ReceiptRow::getProduct, ReceiptRow::getQuantity, Integer::sum));

        Set<Product> productsToApplyDiscount = promotionalProductsQuantityMap.entrySet().stream()
                .filter(e -> e.getValue() >= minQuantityToApplyDiscount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        receipt.getRows().stream()
                .filter(r -> productsToApplyDiscount.contains(r.getProduct()))
                .forEach(r -> {
                    double rowTotalPrice = r.getRowTotalPrice();
                    double discountAmount = rowTotalPrice * discountPercentage / 100.0;
                    r.setRowTotalPriceWithDiscount(rowTotalPrice - discountAmount);
                });

    }

    public String getValidationErrorMessage(String[] args) {

        if (args == null || args.length == 0) {
            return "Can't print receipt. Because no input arguments.";
        }
        if (!validateProductsQuantityArgs(args)) {
            return "Can't print receipt. Wrong input products-quantity arguments format.";
        }
        if (!validateDiscountCardArg(args)) {
            return "Can't print receipt. Invalid discount card arguments.";
        }
        return "";
    }

    private boolean validateProductsQuantityArgs(String[] args) {
        return Stream.of(args)
                .map(String::trim)
                .filter(arg -> !arg.startsWith(DISCOUNT_CARD_KEYWORD_PREFIX))
                .allMatch(arg -> arg.matches("\\d+-\\d+"));
    }

    private boolean validateDiscountCardArg(String[] args) {
        long discountCardArgsCount = Stream.of(args)
                .map(String::trim)
                .filter(arg -> arg.startsWith(DISCOUNT_CARD_KEYWORD_PREFIX))
                .count();
        if (discountCardArgsCount > 1) {
            return false;
        }
        return Stream.of(args)
                .map(String::trim)
                .filter(arg -> arg.startsWith(DISCOUNT_CARD_KEYWORD_PREFIX))
                .allMatch(arg -> arg.matches(DISCOUNT_CARD_KEYWORD_PREFIX + "\\d+"));
    }

    public Receipt createReceiptFromParams(String cashierName, String[] args) {
        Receipt receipt = openReceipt(cashierName);
        Stream.of(args)
                .map(String::trim)
                .filter(arg -> !arg.startsWith(DISCOUNT_CARD_KEYWORD_PREFIX))
                .map(arg -> arg.split("-"))
                .forEach(productQuantityPair -> addPositionToReceipt(receipt,
                        Long.parseLong(productQuantityPair[0]),
                        Integer.parseInt(productQuantityPair[1])));

        Stream.of(args)
                .map(String::trim)
                .filter(arg -> arg.startsWith(DISCOUNT_CARD_KEYWORD_PREFIX))
                .findFirst()
                .map(arg -> arg.split("-")[1])
                .ifPresent(cardId -> applyDiscountCardToReceipt(receipt, Long.parseLong(cardId)));
        return receipt;
    }

}