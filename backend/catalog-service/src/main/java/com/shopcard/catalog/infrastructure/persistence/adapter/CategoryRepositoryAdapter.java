package com.shopcard.catalog.infrastructure.persistence.adapter;

import com.shopcard.catalog.domain.model.Category;
import com.shopcard.catalog.domain.ports.out.CategoryRepositoryPort;
import com.shopcard.catalog.infrastructure.persistence.PersistenceMapper;
import com.shopcard.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.shopcard.catalog.infrastructure.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity jpaEntity = PersistenceMapper.toJpa(category);
        CategoryJpaEntity savedEntity = categoryJpaRepository.save(jpaEntity);
        return PersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryJpaRepository.findByName(name)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryJpaRepository.findBySlug(slug)
                .map(PersistenceMapper::toDomain);
    }
}
