package com.shopcard.order.domain.ports.out;

import com.shopcard.order.domain.model.Order;

public interface EventPublisherPort {
    void publishOrderEvent(String topic, Order order);
}
