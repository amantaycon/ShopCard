package com.shopcard.catalog.infrastructure.persistence.repository;

import com.shopcard.catalog.infrastructure.persistence.entity.ImportJobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImportJobJpaRepository extends JpaRepository<ImportJobJpaEntity, UUID> {
    List<ImportJobJpaEntity> findByShopId(UUID shopId);
}
