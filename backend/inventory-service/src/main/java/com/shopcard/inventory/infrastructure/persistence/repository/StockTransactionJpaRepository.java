package com.shopcard.inventory.infrastructure.persistence.repository;

import com.shopcard.inventory.infrastructure.persistence.entity.StockTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransactionJpaRepository extends JpaRepository<StockTransactionJpaEntity, Long> {
    List<StockTransactionJpaEntity> findByProductId(UUID productId);

    boolean existsByReferenceIdAndTransactionType(String referenceId, String transactionType);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
