package com.shopcard.catalog.domain.service;

import com.shopcard.catalog.domain.model.Category;
import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.in.*;
import com.shopcard.catalog.domain.ports.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService implements
        CreateProductUseCase,
        UpdateProductUseCase,
        GetProductsByShopUseCase,
        SearchProductsUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductSearchPort productSearchPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public Product createProduct(UUID shopId, ProductCommand command) {
        Category category = null;
        if (command.categoryName() != null && !command.categoryName().trim().isEmpty()) {
            String categoryName = command.categoryName().trim();
            String slug = categoryName.toLowerCase().replace(" ", "-");
            category = categoryRepositoryPort.findByName(categoryName)
                    .orElseGet(() -> categoryRepositoryPort.save(Category.builder()
                            .name(categoryName)
                            .slug(slug)
                            .build()));
        }

        Product product = Product.builder()
                .shopId(shopId)
                .name(command.name())
                .description(command.description())
                .sku(command.sku())
                .price(command.price())
                .imageUrl(command.imageUrl())
                .category(category)
                .isAvailable(command.isAvailable() != null ? command.isAvailable() : true)
                .build();

        Product savedProduct = productRepositoryPort.save(product);

        // Sync to search index
        try {
            productSearchPort.save(savedProduct);
        } catch (Exception e) {
            System.err.println("Failed to sync to Elasticsearch index: " + e.getMessage());
        }

        // Publish event to Kafka
        try {
            eventPublisherPort.publishProductEvent("CREATE", savedProduct);
        } catch (Exception e) {
            System.err.println("Failed to publish Kafka product event: " + e.getMessage());
        }

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(UUID shopId, UUID productId, ProductCommand command) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShopId().equals(shopId)) {
            throw new RuntimeException("Unauthorized to modify this product");
        }

        if (command.categoryName() != null && !command.categoryName().trim().isEmpty()) {
            String categoryName = command.categoryName().trim();
            String slug = categoryName.toLowerCase().replace(" ", "-");
            Category category = categoryRepositoryPort.findByName(categoryName)
                    .orElseGet(() -> categoryRepositoryPort.save(Category.builder()
                            .name(categoryName)
                            .slug(slug)
                            .build()));
            product.setCategory(category);
        }

        product.setName(command.name());
        product.setDescription(command.description());
        product.setSku(command.sku());
        product.setPrice(command.price());
        product.setImageUrl(command.imageUrl());
        if (command.isAvailable() != null) {
            product.setIsAvailable(command.isAvailable());
        }

        Product savedProduct = productRepositoryPort.save(product);

        // Sync to search index
        try {
            productSearchPort.save(savedProduct);
        } catch (Exception e) {
            System.err.println("Failed to sync to Elasticsearch index: " + e.getMessage());
        }

        // Publish event to Kafka
        try {
            eventPublisherPort.publishProductEvent("UPDATE", savedProduct);
        } catch (Exception e) {
            System.err.println("Failed to publish Kafka product event: " + e.getMessage());
        }

        return savedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByShop(UUID shopId) {
        return productRepositoryPort.findByShopId(shopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        return productSearchPort.searchProducts(query);
    }
}
