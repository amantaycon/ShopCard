package com.shopcard.shop.domain.ports.in;

import java.util.UUID;

public interface UnfollowShopUseCase {
    void unfollowShop(UUID shopId, UUID customerId);
}
