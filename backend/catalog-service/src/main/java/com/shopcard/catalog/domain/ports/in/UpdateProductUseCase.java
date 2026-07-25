package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.Product;
import java.util.UUID;

public interface UpdateProductUseCase {
    Product updateProduct(UUID shopId, UUID productId, ProductCommand command);
}
