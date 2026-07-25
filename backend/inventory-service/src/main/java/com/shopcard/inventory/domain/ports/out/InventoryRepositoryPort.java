package com.shopcard.inventory.domain.ports.out;

import com.shopcard.inventory.domain.model.Inventory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepositoryPort {
    Inventory save(Inventory inventory);
    List<Inventory> saveAll(List<Inventory> inventories);
    Optional<Inventory> findByProductIdForUpdate(UUID productId);
    List<Inventory> findByShopId(UUID shopId);
    Optional<Inventory> findByProductIdAndShopId(UUID productId, UUID shopId);
}
