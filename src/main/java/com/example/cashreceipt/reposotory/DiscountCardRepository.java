package com.example.cashreceipt.reposotory;

import com.example.cashreceipt.entity.DiscountCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCardRepository extends JpaRepository<DiscountCardEntity, Long> {
}
