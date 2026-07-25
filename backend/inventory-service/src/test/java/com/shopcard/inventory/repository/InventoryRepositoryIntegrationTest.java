package com.shopcard.inventory.repository;

import com.shopcard.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import com.shopcard.inventory.infrastructure.persistence.entity.StockTransactionJpaEntity;
import com.shopcard.inventory.infrastructure.persistence.repository.InventoryJpaRepository;
import com.shopcard.inventory.infrastructure.persistence.repository.StockTransactionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class InventoryRepositoryIntegrationTest {

    @Autowired
    private InventoryJpaRepository inventoryRepository;

    @Autowired
    private StockTransactionJpaRepository stockTransactionRepository;

    @Test
    void lockedProductLookupReturnsInventoryForUpdate() {
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        inventoryRepository.save(InventoryJpaEntity.builder()
                .shopId(shopId)
                .productId(productId)
                .stockQty(8)
                .reservedQty(2)
                .build());

        assertThat(inventoryRepository.findByProductIdForUpdate(productId)).isPresent();
        assertThat(inventoryRepository.findByProductIdAndShopId(productId, shopId)).isPresent();
    }

    @Test
    void stockTransactionPersistsIdempotencyKeyAndLookup() {
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String key = "ORDER_RESERVED:order-1:" + productId;

        stockTransactionRepository.save(StockTransactionJpaEntity.builder()
                .shopId(shopId)
                .productId(productId)
                .transactionType("ORDER_RESERVED")
                .quantity(2)
                .referenceId("order-1")
                .stockBefore(10)
                .stockAfter(10)
                .reservedBefore(0)
                .reservedAfter(2)
                .idempotencyKey(key)
                .build());

        assertThat(stockTransactionRepository.existsByIdempotencyKey(key)).isTrue();
        assertThat(stockTransactionRepository.existsByReferenceIdAndTransactionType("order-1", "ORDER_RESERVED")).isTrue();
    }
}
