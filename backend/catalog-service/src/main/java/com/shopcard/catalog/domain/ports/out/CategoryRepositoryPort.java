package com.shopcard.catalog.domain.ports.out;

import com.shopcard.catalog.domain.model.Category;
import java.util.Optional;

public interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
}
