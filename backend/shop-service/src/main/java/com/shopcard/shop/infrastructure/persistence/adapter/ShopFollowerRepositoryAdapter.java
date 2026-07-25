package com.shopcard.shop.infrastructure.persistence.adapter;

import com.shopcard.shop.domain.model.ShopFollower;
import com.shopcard.shop.domain.ports.out.ShopFollowerRepositoryPort;
import com.shopcard.shop.infrastructure.persistence.entity.ShopFollowerJpaEntity;
import com.shopcard.shop.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.shop.infrastructure.persistence.repository.ShopFollowerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShopFollowerRepositoryAdapter implements ShopFollowerRepositoryPort {

    private final ShopFollowerJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<ShopFollower> findByShopIdAndCustomerId(UUID shopId, UUID customerId) {
        return repository.findByShopIdAndCustomerId(shopId, customerId).map(mapper::toDomain);
    }

    @Override
    public List<ShopFollower> findByCustomerId(UUID customerId) {
        List<ShopFollowerJpaEntity> entities = repository.findByCustomerId(customerId);
        return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByShopId(UUID shopId) {
        return repository.countByShopId(shopId);
    }

    @Override
    public ShopFollower save(ShopFollower follower) {
        ShopFollowerJpaEntity entity = mapper.toJpa(follower);
        ShopFollowerJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(ShopFollower follower) {
        ShopFollowerJpaEntity entity = mapper.toJpa(follower);
        repository.delete(entity);
    }
}
