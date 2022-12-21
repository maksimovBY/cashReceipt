package com.example.cashreceipt.reposotory;

import com.example.cashreceipt.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
