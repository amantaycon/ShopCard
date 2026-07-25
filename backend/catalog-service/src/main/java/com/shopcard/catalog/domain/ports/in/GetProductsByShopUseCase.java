package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.Product;
import java.util.List;
import java.util.UUID;

public interface GetProductsByShopUseCase {
    List<Product> getProductsByShop(UUID shopId);
}
