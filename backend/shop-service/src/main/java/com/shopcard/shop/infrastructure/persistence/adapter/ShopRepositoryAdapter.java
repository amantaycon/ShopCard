package com.shopcard.shop.infrastructure.persistence.adapter;

import com.shopcard.shop.domain.model.Shop;
import com.shopcard.shop.domain.ports.out.ShopRepositoryPort;
import com.shopcard.shop.infrastructure.persistence.entity.ShopJpaEntity;
import com.shopcard.shop.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.shop.infrastructure.persistence.repository.ShopJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShopRepositoryAdapter implements ShopRepositoryPort {

    private final ShopJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<Shop> findByOwnerId(UUID ownerId) {
        return repository.findByOwnerId(ownerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Shop> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Shop save(Shop shop) {
        ShopJpaEntity entity = mapper.toJpa(shop);
        ShopJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Shop> findNearbyShops(double longitude, double latitude, double radiusInMeters) {
        List<ShopJpaEntity> entities = repository.findNearbyShops(longitude, latitude, radiusInMeters);
        return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
