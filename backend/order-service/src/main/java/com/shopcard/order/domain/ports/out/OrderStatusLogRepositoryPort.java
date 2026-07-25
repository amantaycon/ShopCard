package com.shopcard.order.domain.ports.out;

import com.shopcard.order.domain.model.OrderStatusLog;
import java.util.List;
import java.util.UUID;

public interface OrderStatusLogRepositoryPort {
    OrderStatusLog save(OrderStatusLog log);
    List<OrderStatusLog> findByOrderId(UUID orderId);
}
