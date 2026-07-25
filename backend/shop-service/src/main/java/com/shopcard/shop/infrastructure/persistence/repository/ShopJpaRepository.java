package com.shopcard.shop.infrastructure.persistence.repository;

import com.shopcard.shop.infrastructure.persistence.entity.ShopJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, UUID> {

    Optional<ShopJpaEntity> findByOwnerId(UUID ownerId);

    @Query(value = "SELECT * FROM shops s WHERE ST_DWithin(s.coordinates::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :distanceInMeters)", nativeQuery = true)
    List<ShopJpaEntity> findNearbyShops(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("distanceInMeters") double distanceInMeters
    );
}
