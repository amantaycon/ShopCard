package com.shopcard.order.infrastructure.persistence.adapter;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.ports.out.OrderRepositoryPort;
import com.shopcard.order.infrastructure.persistence.PersistenceMapper;
import com.shopcard.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.shopcard.order.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = PersistenceMapper.toJpa(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orderJpaRepository.findByCustomerId(customerId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByShopId(UUID shopId) {
        return orderJpaRepository.findByShopId(shopId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
