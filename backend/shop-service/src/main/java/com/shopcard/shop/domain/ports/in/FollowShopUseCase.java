package com.shopcard.shop.domain.ports.in;

import java.util.UUID;

public interface FollowShopUseCase {
    void followShop(UUID shopId, UUID customerId);
}
