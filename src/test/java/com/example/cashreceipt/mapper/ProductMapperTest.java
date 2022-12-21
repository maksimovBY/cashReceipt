package com.example.cashreceipt.mapper;

import com.example.cashreceipt.dto.Product;
import com.example.cashreceipt.entity.ProductEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    ProductMapper mapper = new ProductMapper();

    @Test
    void toDtoMapNull() {
        Product result = mapper.toDto(null);
        assertNull(result);
    }

    @Test
    void toEntityNull() {
        ProductEntity result = mapper.toEntity(null);
        assertNull(result);
    }
}