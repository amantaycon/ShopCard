package com.shopcard.catalog.infrastructure.persistence;

import com.shopcard.catalog.domain.model.Category;
import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.model.ImportJob;
import com.shopcard.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.shopcard.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.shopcard.catalog.infrastructure.persistence.entity.ImportJobJpaEntity;

public class PersistenceMapper {

    public static Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .parent(toDomain(entity.getParent()))
                .build();
    }

    public static CategoryJpaEntity toJpa(Category domain) {
        if (domain == null) {
            return null;
        }
        return CategoryJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .slug(domain.getSlug())
                .parent(toJpa(domain.getParent()))
                .build();
    }

    public static Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Product.builder()
                .id(entity.getId())
                .shopId(entity.getShopId())
                .category(toDomain(entity.getCategory()))
                .name(entity.getName())
                .description(entity.getDescription())
                .sku(entity.getSku())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .isAvailable(entity.getIsAvailable())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ProductJpaEntity toJpa(Product domain) {
        if (domain == null) {
            return null;
        }
        return ProductJpaEntity.builder()
                .id(domain.getId())
                .shopId(domain.getShopId())
                .category(toJpa(domain.getCategory()))
                .name(domain.getName())
                .description(domain.getDescription())
                .sku(domain.getSku())
                .price(domain.getPrice())
                .imageUrl(domain.getImageUrl())
                .isAvailable(domain.getIsAvailable())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static ImportJob toDomain(ImportJobJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return ImportJob.builder()
                .id(entity.getId())
                .shopId(entity.getShopId())
                .fileName(entity.getFileName())
                .status(entity.getStatus())
                .totalRecords(entity.getTotalRecords())
                .processedRecords(entity.getProcessedRecords())
                .failedRecords(entity.getFailedRecords())
                .errorLog(entity.getErrorLog())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ImportJobJpaEntity toJpa(ImportJob domain) {
        if (domain == null) {
            return null;
        }
        return ImportJobJpaEntity.builder()
                .id(domain.getId())
                .shopId(domain.getShopId())
                .fileName(domain.getFileName())
                .status(domain.getStatus())
                .totalRecords(domain.getTotalRecords())
                .processedRecords(domain.getProcessedRecords())
                .failedRecords(domain.getFailedRecords())
                .errorLog(domain.getErrorLog())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
