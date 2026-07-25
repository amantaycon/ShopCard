package com.shopcard.shop.domain.ports.in;

import com.shopcard.shop.domain.model.Shop;

public record ShopResult(Shop shop, long followerCount) {}
