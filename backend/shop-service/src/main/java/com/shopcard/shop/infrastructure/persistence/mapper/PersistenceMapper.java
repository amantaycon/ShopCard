package com.shopcard.shop.infrastructure.persistence.mapper;

import com.shopcard.shop.domain.model.Coordinates;
import com.shopcard.shop.domain.model.Shop;
import com.shopcard.shop.domain.model.ShopFollower;
import com.shopcard.shop.infrastructure.persistence.entity.ShopFollowerJpaEntity;
import com.shopcard.shop.infrastructure.persistence.entity.ShopJpaEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class PersistenceMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public Shop toDomain(ShopJpaEntity entity) {
        if (entity == null) return null;
        Coordinates coordinates = null;
        if (entity.getCoordinates() != null) {
            coordinates = new Coordinates(entity.getCoordinates().getX(), entity.getCoordinates().getY());
        }

        return Shop.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .description(entity.getDescription())
                .address(entity.getAddress())
                .coordinates(coordinates)
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .logoUrl(entity.getLogoUrl())
                .bannerUrl(entity.getBannerUrl())
                .isVerified(entity.getIsVerified())
                .businessType(entity.getBusinessType())
                .shopType(entity.getShopType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ShopJpaEntity toJpa(Shop domain) {
        if (domain == null) return null;
        Point point = null;
        if (domain.getCoordinates() != null) {
            point = geometryFactory.createPoint(new Coordinate(domain.getCoordinates().longitude(), domain.getCoordinates().latitude()));
        }

        return ShopJpaEntity.builder()
                .id(domain.getId())
                .ownerId(domain.getOwnerId())
                .name(domain.getName())
                .description(domain.getDescription())
                .address(domain.getAddress())
                .coordinates(point)
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .logoUrl(domain.getLogoUrl())
                .bannerUrl(domain.getBannerUrl())
                .isVerified(domain.getIsVerified())
                .businessType(domain.getBusinessType())
                .shopType(domain.getShopType())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ShopFollower toDomain(ShopFollowerJpaEntity entity) {
        if (entity == null) return null;
        return ShopFollower.builder()
                .id(entity.getId())
                .shop(toDomain(entity.getShop()))
                .customerId(entity.getCustomerId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ShopFollowerJpaEntity toJpa(ShopFollower domain) {
        if (domain == null) return null;
        return ShopFollowerJpaEntity.builder()
                .id(domain.getId())
                .shop(toJpa(domain.getShop()))
                .customerId(domain.getCustomerId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
