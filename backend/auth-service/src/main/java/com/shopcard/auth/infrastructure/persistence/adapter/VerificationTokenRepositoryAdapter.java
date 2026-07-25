package com.shopcard.auth.infrastructure.persistence.adapter;

import com.shopcard.auth.domain.model.VerificationToken;
import com.shopcard.auth.domain.ports.out.VerificationTokenRepositoryPort;
import com.shopcard.auth.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import com.shopcard.auth.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.auth.infrastructure.persistence.repository.VerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepositoryPort {

    private final VerificationTokenJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenJpaEntity jpaEntity = mapper.toJpa(token);
        VerificationTokenJpaEntity saved = repository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(VerificationToken token) {
        VerificationTokenJpaEntity jpaEntity = mapper.toJpa(token);
        repository.delete(jpaEntity);
    }
}
