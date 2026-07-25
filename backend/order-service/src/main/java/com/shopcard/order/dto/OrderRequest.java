package com.shopcard.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    @NotNull(message = "Shop ID is required")
    private UUID shopId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemDto> items;
}
