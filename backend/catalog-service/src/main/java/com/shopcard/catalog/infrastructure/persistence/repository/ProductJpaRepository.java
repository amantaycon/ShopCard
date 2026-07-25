package com.shopcard.catalog.infrastructure.persistence.repository;

import com.shopcard.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {
    List<ProductJpaEntity> findByShopId(UUID shopId);
    Optional<ProductJpaEntity> findByShopIdAndSku(UUID shopId, String sku);
}
