package com.shopcard.catalog.infrastructure.persistence.repository;

import com.shopcard.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Integer> {
    Optional<CategoryJpaEntity> findByName(String name);
    Optional<CategoryJpaEntity> findBySlug(String slug);
}
