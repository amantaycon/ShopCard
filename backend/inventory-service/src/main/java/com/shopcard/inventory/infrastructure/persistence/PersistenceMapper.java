package com.shopcard.inventory.infrastructure.persistence;

import com.shopcard.inventory.domain.model.Inventory;
import com.shopcard.inventory.domain.model.StockTransaction;
import com.shopcard.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import com.shopcard.inventory.infrastructure.persistence.entity.StockTransactionJpaEntity;

public class PersistenceMapper {

    public static Inventory toDomain(InventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Inventory.builder()
                .productId(entity.getProductId())
                .shopId(entity.getShopId())
                .stockQty(entity.getStockQty())
                .reservedQty(entity.getReservedQty())
                .version(entity.getVersion())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static InventoryJpaEntity toJpa(Inventory domain) {
        if (domain == null) {
            return null;
        }
        return InventoryJpaEntity.builder()
                .productId(domain.getProductId())
                .shopId(domain.getShopId())
                .stockQty(domain.getStockQty())
                .reservedQty(domain.getReservedQty())
                .version(domain.getVersion())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static StockTransaction toDomain(StockTransactionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return StockTransaction.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .shopId(entity.getShopId())
                .transactionType(entity.getTransactionType())
                .quantity(entity.getQuantity())
                .referenceId(entity.getReferenceId())
                .stockBefore(entity.getStockBefore())
                .stockAfter(entity.getStockAfter())
                .reservedBefore(entity.getReservedBefore())
                .reservedAfter(entity.getReservedAfter())
                .idempotencyKey(entity.getIdempotencyKey())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static StockTransactionJpaEntity toJpa(StockTransaction domain) {
        if (domain == null) {
            return null;
        }
        return StockTransactionJpaEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .shopId(domain.getShopId())
                .transactionType(domain.getTransactionType())
                .quantity(domain.getQuantity())
                .referenceId(domain.getReferenceId())
                .stockBefore(domain.getStockBefore())
                .stockAfter(domain.getStockAfter())
                .reservedBefore(domain.getReservedBefore())
                .reservedAfter(domain.getReservedAfter())
                .idempotencyKey(domain.getIdempotencyKey())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
