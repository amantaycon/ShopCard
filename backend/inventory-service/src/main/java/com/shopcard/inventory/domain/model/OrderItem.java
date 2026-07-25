package com.shopcard.inventory.domain.model;

import java.util.UUID;

public record OrderItem(UUID productId, Integer quantity) {}
