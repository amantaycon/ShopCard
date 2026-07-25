package com.shopcard.inventory.infrastructure.persistence.adapter;

import com.shopcard.inventory.domain.model.Inventory;
import com.shopcard.inventory.domain.ports.out.InventoryRepositoryPort;
import com.shopcard.inventory.infrastructure.persistence.PersistenceMapper;
import com.shopcard.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import com.shopcard.inventory.infrastructure.persistence.repository.InventoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity jpa = PersistenceMapper.toJpa(inventory);
        InventoryJpaEntity saved = inventoryJpaRepository.save(jpa);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Inventory> saveAll(List<Inventory> inventories) {
        List<InventoryJpaEntity> jpaList = inventories.stream()
                .map(PersistenceMapper::toJpa)
                .collect(Collectors.toList());
        List<InventoryJpaEntity> saved = inventoryJpaRepository.saveAll(jpaList);
        return saved.stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findByProductIdForUpdate(UUID productId) {
        return inventoryJpaRepository.findByProductIdForUpdate(productId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Inventory> findByShopId(UUID shopId) {
        return inventoryJpaRepository.findByShopId(shopId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findByProductIdAndShopId(UUID productId, UUID shopId) {
        return inventoryJpaRepository.findByProductIdAndShopId(productId, shopId)
                .map(PersistenceMapper::toDomain);
    }
}
