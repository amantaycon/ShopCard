package com.shopcard.catalog.infrastructure.search.adapter;

import com.shopcard.catalog.domain.model.Category;
import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.out.ProductSearchPort;
import com.shopcard.catalog.infrastructure.search.model.ProductIndex;
import com.shopcard.catalog.infrastructure.search.repository.ProductIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElasticsearchProductSearchAdapter implements ProductSearchPort {

    private final ProductIndexRepository productIndexRepository;

    @Override
    public void save(Product product) {
        ProductIndex doc = ProductIndex.builder()
                .id(product.getId().toString())
                .shopId(product.getShopId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .isAvailable(product.getIsAvailable())
                .build();
        productIndexRepository.save(doc);
    }

    @Override
    public List<Product> searchProducts(String query) {
        List<ProductIndex> indexes = productIndexRepository.findByNameOrDescription(query, query);
        return indexes.stream().map(doc -> Product.builder()
                .id(UUID.fromString(doc.getId()))
                .shopId(UUID.fromString(doc.getShopId()))
                .name(doc.getName())
                .description(doc.getDescription())
                .sku(doc.getSku())
                .price(doc.getPrice())
                .category(doc.getCategoryName() != null ? Category.builder().name(doc.getCategoryName()).build() : null)
                .isAvailable(doc.getIsAvailable())
                .build()
        ).collect(Collectors.toList());
    }
}
