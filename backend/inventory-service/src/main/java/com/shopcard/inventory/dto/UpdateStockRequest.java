package com.shopcard.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateStockRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity difference is required")
    @Positive(message = "Stock-in quantity must be positive")
    private Integer quantity;
}
