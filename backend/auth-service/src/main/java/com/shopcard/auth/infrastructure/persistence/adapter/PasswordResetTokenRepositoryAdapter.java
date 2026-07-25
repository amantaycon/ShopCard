package com.shopcard.auth.infrastructure.persistence.adapter;

import com.shopcard.auth.domain.model.PasswordResetToken;
import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.ports.out.PasswordResetTokenRepositoryPort;
import com.shopcard.auth.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.shopcard.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.shopcard.auth.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.auth.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<PasswordResetToken> findByUser(User user) {
        UserJpaEntity jpaUser = mapper.toJpa(user);
        return repository.findByUser(jpaUser).map(mapper::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity jpaEntity = mapper.toJpa(token);
        PasswordResetTokenJpaEntity saved = repository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(PasswordResetToken token) {
        PasswordResetTokenJpaEntity jpaEntity = mapper.toJpa(token);
        repository.delete(jpaEntity);
    }
}
