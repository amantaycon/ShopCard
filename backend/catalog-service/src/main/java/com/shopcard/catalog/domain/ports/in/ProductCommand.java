package com.shopcard.catalog.domain.ports.in;

import java.math.BigDecimal;

public record ProductCommand(
    String name,
    String description,
    String sku,
    BigDecimal price,
    String imageUrl,
    String categoryName,
    Boolean isAvailable
) {}
