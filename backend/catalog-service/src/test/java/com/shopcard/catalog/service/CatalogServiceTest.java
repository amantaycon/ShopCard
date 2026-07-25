package com.shopcard.catalog.service;

import com.shopcard.catalog.domain.model.Category;
import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.in.ProductCommand;
import com.shopcard.catalog.domain.ports.out.*;
import com.shopcard.catalog.domain.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private ProductSearchPort productSearchPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void createProduct_withNewCategory_shouldSaveCategoryAndProduct() {
        UUID shopId = UUID.randomUUID();
        ProductCommand command = new ProductCommand(
                "Test Product",
                "Description",
                "SKU-123",
                BigDecimal.valueOf(99.99),
                "image.jpg",
                "Electronics",
                true
        );

        Category electronics = Category.builder().id(1).name("Electronics").slug("electronics").build();

        when(categoryRepositoryPort.findByName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(electronics);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(UUID.randomUUID());
            return product;
        });

        Product created = catalogService.createProduct(shopId, command);

        assertThat(created.getName()).isEqualTo("Test Product");
        assertThat(created.getCategory().getName()).isEqualTo("Electronics");
        assertThat(created.getSku()).isEqualTo("SKU-123");
        assertThat(created.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));

        verify(categoryRepositoryPort).save(any(Category.class));
        verify(productRepositoryPort).save(any(Product.class));
        verify(productSearchPort).save(any(Product.class));
        verify(eventPublisherPort).publishProductEvent(eq("CREATE"), any(Product.class));
    }

    @Test
    void updateProduct_validProduct_shouldUpdateProductAndSync() {
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.builder()
                .id(productId)
                .shopId(shopId)
                .name("Old Name")
                .sku("SKU-123")
                .price(BigDecimal.TEN)
                .isAvailable(true)
                .build();

        ProductCommand command = new ProductCommand(
                "New Name",
                "New Desc",
                "SKU-123",
                BigDecimal.valueOf(15.00),
                "new.jpg",
                "Books",
                false
        );

        Category books = Category.builder().id(2).name("Books").slug("books").build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepositoryPort.findByName("Books")).thenReturn(Optional.empty());
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(books);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = catalogService.updateProduct(shopId, productId, command);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(15.00));
        assertThat(updated.getCategory().getName()).isEqualTo("Books");
        assertThat(updated.getIsAvailable()).isFalse();

        verify(productRepositoryPort).save(any(Product.class));
        verify(productSearchPort).save(any(Product.class));
        verify(eventPublisherPort).publishProductEvent(eq("UPDATE"), any(Product.class));
    }

    @Test
    void updateProduct_unauthorizedShop_shouldThrowException() {
        UUID shopId = UUID.randomUUID();
        UUID otherShopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.builder()
                .id(productId)
                .shopId(otherShopId)
                .name("Test Product")
                .build();

        ProductCommand command = new ProductCommand("Name", "Desc", "SKU", BigDecimal.ONE, null, null, true);

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> catalogService.updateProduct(shopId, productId, command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");

        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    void getProductsByShop_shouldReturnList() {
        UUID shopId = UUID.randomUUID();
        Product p = Product.builder().name("Product 1").shopId(shopId).build();

        when(productRepositoryPort.findByShopId(shopId)).thenReturn(List.of(p));

        List<Product> products = catalogService.getProductsByShop(shopId);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Product 1");
    }

    @Test
    void searchProducts_shouldQuerySearchPort() {
        Product p = Product.builder().name("Match").build();

        when(productSearchPort.searchProducts("query")).thenReturn(List.of(p));

        List<Product> results = catalogService.searchProducts("query");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Match");
    }
}
