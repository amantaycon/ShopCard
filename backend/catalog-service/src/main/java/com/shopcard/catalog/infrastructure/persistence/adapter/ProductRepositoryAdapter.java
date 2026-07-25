package com.shopcard.catalog.infrastructure.persistence.adapter;

import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.out.ProductRepositoryPort;
import com.shopcard.catalog.infrastructure.persistence.PersistenceMapper;
import com.shopcard.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.shopcard.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = PersistenceMapper.toJpa(product);
        ProductJpaEntity savedEntity = productJpaRepository.save(jpaEntity);
        return PersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Product> findByShopId(UUID shopId) {
        return productJpaRepository.findByShopId(shopId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findByShopIdAndSku(UUID shopId, String sku) {
        return productJpaRepository.findByShopIdAndSku(shopId, sku)
                .map(PersistenceMapper::toDomain);
    }
}
