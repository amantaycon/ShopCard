package com.shopcard.inventory.domain.ports.out;

import com.shopcard.inventory.domain.model.StockTransaction;
import java.util.List;

public interface StockTransactionRepositoryPort {
    StockTransaction save(StockTransaction transaction);
    List<StockTransaction> saveAll(List<StockTransaction> transactions);
    boolean existsByReferenceIdAndTransactionType(String referenceId, String transactionType);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
