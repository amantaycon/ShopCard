package com.shopcard.order.infrastructure.persistence.adapter;

import com.shopcard.order.domain.model.OrderStatusLog;
import com.shopcard.order.domain.ports.out.OrderStatusLogRepositoryPort;
import com.shopcard.order.infrastructure.persistence.PersistenceMapper;
import com.shopcard.order.infrastructure.persistence.entity.OrderStatusLogJpaEntity;
import com.shopcard.order.infrastructure.persistence.repository.OrderStatusLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderStatusLogRepositoryAdapter implements OrderStatusLogRepositoryPort {

    private final OrderStatusLogJpaRepository logJpaRepository;

    @Override
    public OrderStatusLog save(OrderStatusLog log) {
        OrderStatusLogJpaEntity entity = PersistenceMapper.toJpa(log);
        OrderStatusLogJpaEntity saved = logJpaRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public List<OrderStatusLog> findByOrderId(UUID orderId) {
        return logJpaRepository.findByOrderId(orderId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
