package com.shopcard.inventory.domain.ports.out;

import java.util.UUID;

public interface EventPublisherPort {
    void publishInventoryResult(String topic, String orderId, UUID shopId, String status, String reason);
}
