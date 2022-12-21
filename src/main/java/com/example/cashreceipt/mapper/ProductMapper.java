package com.example.cashreceipt.mapper;

import com.example.cashreceipt.dto.Product;
import com.example.cashreceipt.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDto(ProductEntity productEntity) {
        if (productEntity == null) {
            return null;
        }
        return Product.builder()
            .id(productEntity.getId())
            .description(productEntity.getDescription())
            .promotional(productEntity.isPromotional())
            .price(productEntity.getPrice())
            .build();
    }

    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        return ProductEntity.builder()
            .id(product.getId())
            .description(product.getDescription())
            .promotional(product.isPromotional())
            .price(product.getPrice())
            .build();
    }

}
