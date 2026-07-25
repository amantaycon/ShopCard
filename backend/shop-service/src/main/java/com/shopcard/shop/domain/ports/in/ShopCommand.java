package com.shopcard.shop.domain.ports.in;

public record ShopCommand(
    String name,
    String description,
    String address,
    double latitude,
    double longitude,
    String phone,
    String email,
    String logoUrl,
    String bannerUrl,
    String businessType,
    String shopType
) {}
