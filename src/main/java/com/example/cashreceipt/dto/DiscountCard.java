package com.example.cashreceipt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscountCard {

    private long id;
    private double discountPercentage;
    private String customerName;

}
