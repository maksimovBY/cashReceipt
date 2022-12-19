package com.example.cashreceipt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {

    private long id;
    private String description;
    private double price;
    private boolean promotional;

}
