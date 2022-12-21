package com.example.cashreceipt.mapper;

import com.example.cashreceipt.dto.DiscountCard;
import com.example.cashreceipt.entity.DiscountCardEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscountCardMapperTest {

    DiscountCardMapper mapper = new DiscountCardMapper();

    @Test
    void toDtoMapNull() {
        DiscountCard result = mapper.toDto(null);
        assertNull(result);
    }

    @Test
    void toEntityNull() {
        DiscountCardEntity result = mapper.toEntity(null);
        assertNull(result);
    }
}