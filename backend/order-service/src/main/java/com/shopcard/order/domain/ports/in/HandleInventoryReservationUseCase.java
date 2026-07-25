package com.shopcard.order.domain.ports.in;

import java.util.UUID;

public interface HandleInventoryReservationUseCase {
    void handleInventoryReservation(UUID orderId, String status, String reason);
}
