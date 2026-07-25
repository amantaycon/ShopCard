package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.Product;
import java.util.List;

public interface SearchProductsUseCase {
    List<Product> searchProducts(String query);
}
