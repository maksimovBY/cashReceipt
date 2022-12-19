package com.example.cashreceipt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiptRow {

    private Product product;
    private int quantity;
    private double rowTotalPrice;
    private Double rowTotalPriceWithDiscount;

}
