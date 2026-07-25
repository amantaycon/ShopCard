package com.shopcard.inventory.service;

import com.shopcard.inventory.domain.model.Inventory;
import com.shopcard.inventory.domain.model.OrderItem;
import com.shopcard.inventory.domain.model.StockTransaction;
import com.shopcard.inventory.domain.ports.out.EventPublisherPort;
import com.shopcard.inventory.domain.ports.out.InventoryRepositoryPort;
import com.shopcard.inventory.domain.ports.out.StockTransactionRepositoryPort;
import com.shopcard.inventory.domain.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private StockTransactionRepositoryPort stockTransactionRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void updateStock_newProduct_shouldSaveInventoryAndTransaction() {
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventory result = inventoryService.updateStock(shopId, productId, 10, "STOCK_IN", "MANUAL_ENTRY");

        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getStockQty()).isEqualTo(10);
        assertThat(result.getReservedQty()).isZero();

        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(stockTransactionRepositoryPort).save(any(StockTransaction.class));
    }

    @Test
    void updateStock_insufficientStock_shouldThrowException() {
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Inventory existing = Inventory.builder()
                .productId(productId)
                .shopId(shopId)
                .stockQty(5)
                .reservedQty(0)
                .build();

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> inventoryService.updateStock(shopId, productId, -10, "MANUAL_ADJUST", "MANUAL_ENTRY"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    void reserveStock_sufficientStock_shouldSucceed() {
        String orderId = "order-123";
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 2);

        Inventory inv = Inventory.builder()
                .productId(productId)
                .shopId(shopId)
                .stockQty(10)
                .reservedQty(3)
                .build();

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inv));
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RESERVED")).thenReturn(false);

        inventoryService.reserveStock(orderId, shopId, List.of(item));

        assertThat(inv.getReservedQty()).isEqualTo(5);
        verify(inventoryRepositoryPort).saveAll(anyList());
        verify(stockTransactionRepositoryPort).saveAll(anyList());
        verify(eventPublisherPort).publishInventoryResult(eq("inventory.reserved"), eq(orderId), eq(shopId), eq("SUCCESS"), eq(""));
    }

    @Test
    void reserveStock_outOfStock_shouldFailAndPublishEvent() {
        String orderId = "order-123";
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 5);

        Inventory inv = Inventory.builder()
                .productId(productId)
                .shopId(shopId)
                .stockQty(5)
                .reservedQty(3)
                .build(); // Available: 2, Required: 5

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inv));

        inventoryService.reserveStock(orderId, shopId, List.of(item));

        verify(inventoryRepositoryPort, never()).saveAll(anyList());
        verify(eventPublisherPort).publishInventoryResult(eq("inventory.failed"), eq(orderId), eq(shopId), eq("FAILED"), anyString());
    }

    @Test
    void releaseStock_withDeduct_shouldReduceStockAndReservation() {
        String orderId = "order-123";
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 3);

        Inventory inv = Inventory.builder()
                .productId(productId)
                .shopId(shopId)
                .stockQty(10)
                .reservedQty(5)
                .build();

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inv));
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_DEDUCTED")).thenReturn(false);
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RELEASED")).thenReturn(false);
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RESERVED")).thenReturn(true);
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.releaseStock(orderId, shopId, List.of(item), true);

        assertThat(inv.getReservedQty()).isEqualTo(2);
        assertThat(inv.getStockQty()).isEqualTo(7);

        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(stockTransactionRepositoryPort).save(any(StockTransaction.class));
    }

    @Test
    void releaseStock_withoutDeduct_shouldReduceOnlyReservation() {
        String orderId = "order-123";
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 3);

        Inventory inv = Inventory.builder()
                .productId(productId)
                .shopId(shopId)
                .stockQty(10)
                .reservedQty(5)
                .build();

        when(inventoryRepositoryPort.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inv));
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RELEASED")).thenReturn(false);
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_DEDUCTED")).thenReturn(false);
        when(stockTransactionRepositoryPort.existsByReferenceIdAndTransactionType(orderId, "ORDER_RESERVED")).thenReturn(true);
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.releaseStock(orderId, shopId, List.of(item), false);

        assertThat(inv.getReservedQty()).isEqualTo(2);
        assertThat(inv.getStockQty()).isEqualTo(10);

        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(stockTransactionRepositoryPort).save(any(StockTransaction.class));
    }
}
