package com.shopcard.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private UUID productId;
    private UUID shopId;
    private Integer stockQty;
    private Integer reservedQty;
    private ZonedDateTime updatedAt;
}
