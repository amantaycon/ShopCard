package com.shopcard.inventory.domain.ports.in;

import com.shopcard.inventory.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface ReserveStockUseCase {
    void reserveStock(String orderId, UUID shopId, List<OrderItem> items);
}
