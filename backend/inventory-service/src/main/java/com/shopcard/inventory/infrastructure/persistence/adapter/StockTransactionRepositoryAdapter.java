package com.shopcard.inventory.infrastructure.persistence.adapter;

import com.shopcard.inventory.domain.model.StockTransaction;
import com.shopcard.inventory.domain.ports.out.StockTransactionRepositoryPort;
import com.shopcard.inventory.infrastructure.persistence.PersistenceMapper;
import com.shopcard.inventory.infrastructure.persistence.entity.StockTransactionJpaEntity;
import com.shopcard.inventory.infrastructure.persistence.repository.StockTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockTransactionRepositoryAdapter implements StockTransactionRepositoryPort {

    private final StockTransactionJpaRepository stockTransactionJpaRepository;

    @Override
    public StockTransaction save(StockTransaction transaction) {
        StockTransactionJpaEntity jpa = PersistenceMapper.toJpa(transaction);
        StockTransactionJpaEntity saved = stockTransactionJpaRepository.save(jpa);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public List<StockTransaction> saveAll(List<StockTransaction> transactions) {
        List<StockTransactionJpaEntity> jpaList = transactions.stream()
                .map(PersistenceMapper::toJpa)
                .collect(Collectors.toList());
        List<StockTransactionJpaEntity> saved = stockTransactionJpaRepository.saveAll(jpaList);
        return saved.stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByReferenceIdAndTransactionType(String referenceId, String transactionType) {
        return stockTransactionJpaRepository.existsByReferenceIdAndTransactionType(referenceId, transactionType);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return stockTransactionJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }
}
