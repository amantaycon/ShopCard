package com.shopcard.shop.domain.ports.out;

import com.shopcard.shop.domain.model.Shop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepositoryPort {
    Optional<Shop> findByOwnerId(UUID ownerId);
    Optional<Shop> findById(UUID id);
    Shop save(Shop shop);
    List<Shop> findNearbyShops(double longitude, double latitude, double radiusInMeters);
}
