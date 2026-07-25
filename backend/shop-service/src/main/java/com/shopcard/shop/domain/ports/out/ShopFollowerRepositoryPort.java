package com.shopcard.shop.domain.ports.out;

import com.shopcard.shop.domain.model.ShopFollower;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopFollowerRepositoryPort {
    Optional<ShopFollower> findByShopIdAndCustomerId(UUID shopId, UUID customerId);
    List<ShopFollower> findByCustomerId(UUID customerId);
    long countByShopId(UUID shopId);
    ShopFollower save(ShopFollower follower);
    void delete(ShopFollower follower);
}
