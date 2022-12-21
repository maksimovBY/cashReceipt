package com.example.cashreceipt.mapper;

import com.example.cashreceipt.dto.DiscountCard;
import com.example.cashreceipt.entity.DiscountCardEntity;
import org.springframework.stereotype.Component;

@Component
public class DiscountCardMapper {

    public DiscountCard toDto(DiscountCardEntity discountCardEntity) {
        if (discountCardEntity == null) {
            return null;
        }
        return DiscountCard.builder()
            .id(discountCardEntity.getId())
            .customerName(discountCardEntity.getCustomerName())
            .discountPercentage(discountCardEntity.getDiscountPercentage())
            .build();
    }

    public DiscountCardEntity toEntity(DiscountCard discountCard) {
        if (discountCard == null) {
            return null;
        }
        return DiscountCardEntity.builder()
            .id(discountCard.getId())
            .customerName(discountCard.getCustomerName())
            .discountPercentage(discountCard.getDiscountPercentage())
            .build();
    }

}
