package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.Order;
import java.util.List;
import java.util.UUID;

public interface GetOrderUseCase {
    Order getOrder(UUID id);
    List<Order> getCustomerOrders(UUID customerId);
    List<Order> getShopOrders(UUID shopId);
}
