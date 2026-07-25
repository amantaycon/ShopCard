package com.shopcard.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID shopId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    
    // Pick up OTP (Only returned plain text upon creation, hashed thereafter)
    private String pickupCodePlain;
}
