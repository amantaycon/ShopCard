package com.shopcard.inventory.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {
    private Long id;
    private UUID productId;
    private UUID shopId;
    private String transactionType;
    private Integer quantity;
    private String referenceId;
    private Integer stockBefore;
    private Integer stockAfter;
    private Integer reservedBefore;
    private Integer reservedAfter;
    private String idempotencyKey;
    private ZonedDateTime createdAt;
}
