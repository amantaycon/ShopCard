package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.Product;
import java.util.UUID;

public interface CreateProductUseCase {
    Product createProduct(UUID shopId, ProductCommand command);
}
