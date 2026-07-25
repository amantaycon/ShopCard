package com.shopcard.shop.domain.ports.in;

import java.util.UUID;

public interface GetShopUseCase {
    ShopResult getShop(UUID shopId);
}
