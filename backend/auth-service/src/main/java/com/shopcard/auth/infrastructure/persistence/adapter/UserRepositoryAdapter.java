package com.shopcard.auth.infrastructure.persistence.adapter;

import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.ports.out.UserRepositoryPort;
import com.shopcard.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.shopcard.auth.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.auth.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = mapper.toJpa(user);
        UserJpaEntity saved = repository.save(jpaEntity);
        return mapper.toDomain(saved);
    }
}
