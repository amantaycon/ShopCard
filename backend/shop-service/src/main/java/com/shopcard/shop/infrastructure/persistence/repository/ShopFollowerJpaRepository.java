package com.shopcard.shop.infrastructure.persistence.repository;

import com.shopcard.shop.infrastructure.persistence.entity.ShopFollowerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopFollowerJpaRepository extends JpaRepository<ShopFollowerJpaEntity, Long> {
    Optional<ShopFollowerJpaEntity> findByShopIdAndCustomerId(UUID shopId, UUID customerId);
    List<ShopFollowerJpaEntity> findByCustomerId(UUID customerId);
    long countByShopId(UUID shopId);
}
