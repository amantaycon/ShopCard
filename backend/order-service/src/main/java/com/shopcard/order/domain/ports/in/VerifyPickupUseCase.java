package com.shopcard.order.domain.ports.in;

import com.shopcard.order.domain.model.Order;
import java.util.UUID;

public interface VerifyPickupUseCase {
    Order verifyPickupAndComplete(UUID shopId, UUID orderId, String plainCode);
}
