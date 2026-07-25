package com.shopcard.inventory.domain.service;

import com.shopcard.inventory.domain.model.Inventory;
import com.shopcard.inventory.domain.model.OrderItem;
import com.shopcard.inventory.domain.model.StockTransaction;
import com.shopcard.inventory.domain.ports.in.*;
import com.shopcard.inventory.domain.ports.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryService implements
        UpdateStockUseCase,
        ReserveStockUseCase,
        ReleaseStockUseCase,
        GetInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final StockTransactionRepositoryPort stockTransactionRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public Inventory updateStock(UUID shopId, UUID productId, int quantity, String transactionType, String referenceId) {
        validateStockMutation(shopId, productId, quantity, transactionType);

        Inventory inventory = inventoryRepositoryPort.findByProductIdForUpdate(productId)
                .orElseGet(() -> Inventory.builder()
                        .productId(productId)
                        .shopId(shopId)
                        .stockQty(0)
                        .reservedQty(0)
                        .build());

        if (!inventory.getShopId().equals(shopId)) {
            throw new RuntimeException("Product inventory does not belong to this shop");
        }

        int stockBefore = inventory.getStockQty();
        int reservedBefore = inventory.getReservedQty();
        int newQty = inventory.getStockQty() + quantity;
        if (newQty < 0) {
            throw new RuntimeException("Insufficient stock. Cannot update by: " + quantity);
        }
        if (newQty < inventory.getReservedQty()) {
            throw new RuntimeException("Stock cannot be reduced below reserved quantity");
        }

        inventory.setStockQty(newQty);
        Inventory saved = inventoryRepositoryPort.save(inventory);

        saveTransaction(saved, transactionType, quantity, referenceId,
                stockBefore, saved.getStockQty(), reservedBefore, saved.getReservedQty(),
                manualIdempotencyKey(transactionType, referenceId, productId));

        return saved;
    }

    @Override
    @Transactional
    public void reserveStock(String orderId, UUID shopId, List<OrderItem> items) {
        try {
            validateOrderEvent(orderId, shopId, items);
        } catch (RuntimeException e) {
            eventPublisherPort.publishInventoryResult("inventory.failed", orderId, shopId, "FAILED", e.getMessage());
            return;
        }

        if (stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RESERVED")) {
            eventPublisherPort.publishInventoryResult("inventory.reserved", orderId, shopId, "SUCCESS", "");
            return;
        }

        List<Inventory> updatedInventories = new ArrayList<>();
        List<StockTransaction> transactions = new ArrayList<>();

        boolean failed = false;
        String reason = "";

        for (OrderItem item : items) {
            Inventory inv = inventoryRepositoryPort.findByProductIdForUpdate(item.productId()).orElse(null);
            if (inv == null) {
                failed = true;
                reason = "Inventory not found for product: " + item.productId();
                break;
            }
            if (!inv.getShopId().equals(shopId)) {
                failed = true;
                reason = "Product does not belong to shop: " + item.productId();
                break;
            }
            if ((inv.getStockQty() - inv.getReservedQty()) < item.quantity()) {
                failed = true;
                reason = "Out of stock for product: " + inv.getProductId();
                break;
            }

            int stockBefore = inv.getStockQty();
            int reservedBefore = inv.getReservedQty();
            inv.setReservedQty(inv.getReservedQty() + item.quantity());
            updatedInventories.add(inv);

            transactions.add(StockTransaction.builder()
                    .productId(item.productId())
                    .shopId(shopId)
                    .transactionType("ORDER_RESERVED")
                    .quantity(item.quantity())
                    .referenceId(orderId)
                    .stockBefore(stockBefore)
                    .stockAfter(inv.getStockQty())
                    .reservedBefore(reservedBefore)
                    .reservedAfter(inv.getReservedQty())
                    .idempotencyKey(idempotencyKey("ORDER_RESERVED", orderId, item.productId()))
                    .build());
        }

        if (failed) {
            eventPublisherPort.publishInventoryResult("inventory.failed", orderId, shopId, "FAILED", reason);
        } else {
            inventoryRepositoryPort.saveAll(updatedInventories);
            stockTransactionRepositoryPort.saveAll(transactions);

            eventPublisherPort.publishInventoryResult("inventory.reserved", orderId, shopId, "SUCCESS", "");
        }
    }

    @Override
    @Transactional
    public void releaseStock(String orderId, UUID shopId, List<OrderItem> items, boolean deduct) {
        validateOrderEvent(orderId, shopId, items);

        String txType = deduct ? "ORDER_DEDUCTED" : "ORDER_RELEASED";
        String oppositeTerminalTxType = deduct ? "ORDER_RELEASED" : "ORDER_DEDUCTED";
        if (stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, txType)) {
            return;
        }
        if (stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, oppositeTerminalTxType)) {
            return;
        }
        if (!stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RESERVED")) {
            return;
        }

        for (OrderItem item : items) {
            Inventory inv = inventoryRepositoryPort.findByProductIdForUpdate(item.productId()).orElse(null);
            if (inv == null) continue;
            if (!inv.getShopId().equals(shopId)) {
                throw new RuntimeException("Product does not belong to shop: " + item.productId());
            }

            int stockBefore = inv.getStockQty();
            int reservedBefore = inv.getReservedQty();
            int reservedToRelease = Math.min(inv.getReservedQty(), item.quantity());
            inv.setReservedQty(inv.getReservedQty() - reservedToRelease);

            if (deduct) {
                int stockToDeduct = Math.min(inv.getStockQty(), reservedToRelease);
                inv.setStockQty(inv.getStockQty() - stockToDeduct);
            }

            Inventory saved = inventoryRepositoryPort.save(inv);

            saveTransaction(saved, txType, reservedToRelease, orderId,
                    stockBefore, saved.getStockQty(), reservedBefore, saved.getReservedQty(),
                    idempotencyKey(txType, orderId, item.productId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getInventoryByShop(UUID shopId) {
        return inventoryRepositoryPort.findByShopId(shopId);
    }

    private void validateStockMutation(UUID shopId, UUID productId, int quantity, String transactionType) {
        if (shopId == null) {
            throw new RuntimeException("Shop ID is required");
        }
        if (productId == null) {
            throw new RuntimeException("Product ID is required");
        }
        if (transactionType == null || transactionType.isBlank()) {
            throw new RuntimeException("Transaction type is required");
        }
        if ("STOCK_IN".equals(transactionType) && quantity <= 0) {
            throw new RuntimeException("Stock-in quantity must be positive");
        }
    }

    private void validateOrderEvent(String orderId, UUID shopId, List<OrderItem> items) {
        if (orderId == null || orderId.isBlank()) {
            throw new RuntimeException("Order ID is required");
        }
        if (shopId == null) {
            throw new RuntimeException("Shop ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        Set<UUID> productIds = new HashSet<>();
        for (OrderItem item : items) {
            if (item == null || item.productId() == null) {
                throw new RuntimeException("Order item product ID is required");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new RuntimeException("Order item quantity must be positive");
            }
            if (!productIds.add(item.productId())) {
                throw new RuntimeException("Duplicate product in order event: " + item.productId());
            }
        }
    }

    private void saveTransaction(Inventory inventory, String transactionType, int quantity, String referenceId,
                                 int stockBefore, int stockAfter, int reservedBefore, int reservedAfter,
                                 String idempotencyKey) {
        if (idempotencyKey != null && stockTransactionRepositoryPort.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        stockTransactionRepositoryPort.save(StockTransaction.builder()
                .productId(inventory.getProductId())
                .shopId(inventory.getShopId())
                .transactionType(transactionType)
                .quantity(quantity)
                .referenceId(referenceId)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .reservedBefore(reservedBefore)
                .reservedAfter(reservedAfter)
                .idempotencyKey(Objects.requireNonNullElseGet(idempotencyKey, () -> UUID.randomUUID().toString()))
                .build());
    }

    private String manualIdempotencyKey(String transactionType, String referenceId, UUID productId) {
        if (referenceId == null || referenceId.isBlank() || "MANUAL_ENTRY".equals(referenceId)) {
            return null;
        }
        return idempotencyKey(transactionType, referenceId, productId);
    }

    private String idempotencyKey(String transactionType, String referenceId, UUID productId) {
        return transactionType + ":" + referenceId + ":" + productId;
    }
}
