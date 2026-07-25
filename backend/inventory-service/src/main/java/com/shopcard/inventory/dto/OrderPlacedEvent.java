package com.shopcard.inventory.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderPlacedEvent {
    private String orderId;
    private UUID shopId;
    private UUID customerId;
    private List<OrderItemDto> items;
}
