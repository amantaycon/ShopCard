package com.shopcard.shop.domain.ports.in;

import java.util.UUID;

public interface CreateOrUpdateShopUseCase {
    ShopResult createOrUpdateShop(UUID ownerId, ShopCommand command);
}
