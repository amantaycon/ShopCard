package com.shopcard.catalog.domain.ports.out;

import com.shopcard.catalog.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findByShopId(UUID shopId);
    Optional<Product> findByShopIdAndSku(UUID shopId, String sku);
}
