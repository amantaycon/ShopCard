package com.shopcard.catalog.domain.ports.out;

import com.shopcard.catalog.domain.model.Product;
import java.util.List;

public interface ProductSearchPort {
    void save(Product product);
    List<Product> searchProducts(String query);
}
