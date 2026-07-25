package com.shopcard.order.infrastructure.persistence.repository;

import com.shopcard.order.infrastructure.persistence.entity.OrderStatusLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusLogJpaRepository extends JpaRepository<OrderStatusLogJpaEntity, Long> {
    List<OrderStatusLogJpaEntity> findByOrderId(UUID orderId);
}
