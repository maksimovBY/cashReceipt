package com.example.cashreceipt.service;

import com.example.cashreceipt.dto.Receipt;
import com.example.cashreceipt.dto.ReceiptRow;
import com.example.cashreceipt.dto.DiscountCard;
import com.example.cashreceipt.dto.Product;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Utils {

    public static Receipt getReceipt() {
        return Receipt.builder()
                .cashierName("Cashier 1")
                .id(1L)
                .date(LocalDate.of(2020, 1, 1))
                .time(LocalTime.of(10, 10))
                .rows(List.of(
                        ReceiptRow.builder()
                                .product(Product.builder()
                                        .id(1L)
                                        .price(1.2)
                                        .promotional(false)
                                        .description("Product 1")
                                        .build())
                                .quantity(1)
                                .rowTotalPrice(1.2)
                                .build(),
                        ReceiptRow.builder()
                                .product(Product.builder()
                                        .id(2L)
                                        .price(2)
                                        .promotional(true)
                                        .description("Product 2")
                                        .build())
                                .quantity(6)
                                .rowTotalPrice(12)
                                .rowTotalPriceWithDiscount(10.8)
                                .build(),
                        ReceiptRow.builder()
                                .product(Product.builder()
                                        .id(3L)
                                        .price(3.33)
                                        .promotional(false)
                                        .description("Product 3")
                                        .build())
                                .quantity(1)
                                .rowTotalPrice(3.33)
                                .build(),
                        ReceiptRow.builder()
                                .product(Product.builder()
                                        .id(4L)
                                        .price(0.17)
                                        .promotional(true)
                                        .description("Product 4")
                                        .build())
                                .quantity(4)
                                .rowTotalPrice(0.68)
                                .build()
                ))
                .discountCard(DiscountCard.builder()
                        .customerName("cust name")
                        .discountPercentage(1)
                        .id(1L)
                        .build())
                .totalPrice(15.85)
                .discountAmount(0.16)
                .totalPriceWithoutDiscount(16.01)
                .build();
    }

}
