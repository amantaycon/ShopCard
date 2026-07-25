package com.shopcard.order.domain.ports.out;

import com.shopcard.order.domain.model.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findByShopId(UUID shopId);
}
