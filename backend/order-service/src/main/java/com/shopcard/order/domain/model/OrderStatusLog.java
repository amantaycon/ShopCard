package com.shopcard.order.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLog {
    private Long id;
    private UUID orderId;
    private String fromStatus;
    private String toStatus;
    private String remarks;
    private UUID changedBy;
    private ZonedDateTime createdAt;
}
