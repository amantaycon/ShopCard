package com.shopcard.inventory.domain.ports.in;

import com.shopcard.inventory.domain.model.Inventory;
import java.util.UUID;

public interface UpdateStockUseCase {
    Inventory updateStock(UUID shopId, UUID productId, int quantity, String transactionType, String referenceId);
}
