package com.shopcard.inventory.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    private UUID productId;
    private UUID shopId;
    @Builder.Default
    private Integer stockQty = 0;
    @Builder.Default
    private Integer reservedQty = 0;
    private Long version;
    private ZonedDateTime updatedAt;
}
