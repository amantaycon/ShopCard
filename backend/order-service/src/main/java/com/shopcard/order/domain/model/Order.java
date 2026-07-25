package com.shopcard.order.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private UUID id;
    private UUID customerId;
    private UUID shopId;
    private BigDecimal totalAmount;
    private String status;
    private String pickupCodeHash;
    private ZonedDateTime pickupCodeExpiry;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
