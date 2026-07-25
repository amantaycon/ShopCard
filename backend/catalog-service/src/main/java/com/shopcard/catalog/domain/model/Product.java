package com.shopcard.catalog.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    private UUID id;
    private UUID shopId;
    private Category category;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String imageUrl;
    @Builder.Default
    private Boolean isAvailable = true;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
