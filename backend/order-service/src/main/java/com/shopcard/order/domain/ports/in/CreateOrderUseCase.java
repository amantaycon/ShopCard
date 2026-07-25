package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface CreateOrderUseCase {
    OrderResult createOrder(UUID customerId, UUID shopId, List<OrderItem> items);
}
