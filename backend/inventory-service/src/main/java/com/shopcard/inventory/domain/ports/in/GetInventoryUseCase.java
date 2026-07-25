package com.shopcard.inventory.domain.ports.in;

import com.shopcard.inventory.domain.model.Inventory;
import java.util.List;
import java.util.UUID;

public interface GetInventoryUseCase {
    List<Inventory> getInventoryByShop(UUID shopId);
}
