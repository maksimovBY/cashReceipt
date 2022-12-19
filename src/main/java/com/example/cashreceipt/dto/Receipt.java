package com.example.cashreceipt.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Data
@Builder(toBuilder = true)
public class Receipt {

    private long id;
    private String cashierName;
    private LocalDate date;
    private LocalTime time;
    private List<ReceiptRow> rows;
    private DiscountCard discountCard;
    private double totalPriceWithoutDiscount;
    private double discountAmount;
    private double totalPrice;

}
