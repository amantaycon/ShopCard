package com.shopcard.inventory.domain.ports.in;

import com.shopcard.inventory.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface ReleaseStockUseCase {
    void releaseStock(String orderId, UUID shopId, List<OrderItem> items, boolean deduct);
}
