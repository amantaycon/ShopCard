package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.Order;
import java.util.UUID;

public interface CancelOrderUseCase {
    Order cancelOrder(UUID actorId, UUID orderId);
}
