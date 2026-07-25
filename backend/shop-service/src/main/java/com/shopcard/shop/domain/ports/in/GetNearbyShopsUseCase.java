package com.shopcard.shop.domain.ports.in;

import java.util.List;

public interface GetNearbyShopsUseCase {
    List<ShopResult> getNearbyShops(double longitude, double latitude, double radiusInMeters);
}
