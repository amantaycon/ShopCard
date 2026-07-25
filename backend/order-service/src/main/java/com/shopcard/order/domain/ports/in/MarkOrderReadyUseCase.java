package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.Order;
import java.util.UUID;

public interface MarkOrderReadyUseCase {
    Order markAsReadyForPickup(UUID shopId, UUID orderId);
}
