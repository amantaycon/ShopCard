package com.shopcard.shop.domain.service;

import com.shopcard.shop.domain.model.Coordinates;
import com.shopcard.shop.domain.model.Shop;
import com.shopcard.shop.domain.model.ShopFollower;
import com.shopcard.shop.domain.ports.in.*;
import com.shopcard.shop.domain.ports.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService implements
        CreateOrUpdateShopUseCase,
        GetShopUseCase,
        GetNearbyShopsUseCase,
        FollowShopUseCase,
        UnfollowShopUseCase {

    private final ShopRepositoryPort shopRepositoryPort;
    private final ShopFollowerRepositoryPort shopFollowerRepositoryPort;

    @Override
    public ShopResult createOrUpdateShop(UUID ownerId, ShopCommand command) {
        Shop shop = shopRepositoryPort.findByOwnerId(ownerId)
                .orElse(Shop.builder().build());

        shop.setOwnerId(ownerId);
        shop.setName(command.name());
        shop.setDescription(command.description());
        shop.setAddress(command.address());
        shop.setCoordinates(new Coordinates(command.longitude(), command.latitude()));
        shop.setPhone(command.phone());
        shop.setEmail(command.email());
        shop.setLogoUrl(command.logoUrl());
        shop.setBannerUrl(command.bannerUrl());
        shop.setBusinessType(command.businessType());
        shop.setShopType(command.shopType());

        Shop saved = shopRepositoryPort.save(shop);
        long count = shopFollowerRepositoryPort.countByShopId(saved.getId());
        return new ShopResult(saved, count);
    }

    @Override
    public ShopResult getShop(UUID shopId) {
        Shop shop = shopRepositoryPort.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        long count = shopFollowerRepositoryPort.countByShopId(shop.getId());
        return new ShopResult(shop, count);
    }

    @Override
    public List<ShopResult> getNearbyShops(double longitude, double latitude, double radiusInMeters) {
        List<Shop> shops = shopRepositoryPort.findNearbyShops(longitude, latitude, radiusInMeters);
        return shops.stream().map(shop -> {
            long count = shopFollowerRepositoryPort.countByShopId(shop.getId());
            return new ShopResult(shop, count);
        }).collect(Collectors.toList());
    }

    @Override
    public void followShop(UUID shopId, UUID customerId) {
        Shop shop = shopRepositoryPort.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId).isPresent()) {
            return; // Already following
        }

        ShopFollower follower = ShopFollower.builder()
                .shop(shop)
                .customerId(customerId)
                .build();
        shopFollowerRepositoryPort.save(follower);
    }

    @Override
    public void unfollowShop(UUID shopId, UUID customerId) {
        ShopFollower follower = shopFollowerRepositoryPort.findByShopIdAndCustomerId(shopId, customerId)
                .orElseThrow(() -> new RuntimeException("Not following this shop"));
        shopFollowerRepositoryPort.delete(follower);
    }
}
