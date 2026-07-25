package com.shopcard.notification.infrastructure.persistence.adapter;

import com.shopcard.notification.domain.model.Notification;
import com.shopcard.notification.domain.ports.out.NotificationRepositoryPort;
import com.shopcard.notification.infrastructure.persistence.PersistenceMapper;
import com.shopcard.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import com.shopcard.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository repository;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = PersistenceMapper.toJpa(notification);
        NotificationJpaEntity saved = repository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return repository.findById(id)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Notification> findByUserIdOrderByTimestampDesc(UUID userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
