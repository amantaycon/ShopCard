package com.shopcard.inventory.infrastructure.persistence.repository;

import com.shopcard.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, UUID> {
    List<InventoryJpaEntity> findByShopId(UUID shopId);

    Optional<InventoryJpaEntity> findByProductIdAndShopId(UUID productId, UUID shopId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryJpaEntity i where i.productId = :productId")
    Optional<InventoryJpaEntity> findByProductIdForUpdate(@Param("productId") UUID productId);
}
