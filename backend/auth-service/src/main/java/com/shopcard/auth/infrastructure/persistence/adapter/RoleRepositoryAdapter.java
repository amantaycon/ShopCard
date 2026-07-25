package com.shopcard.auth.infrastructure.persistence.adapter;

import com.shopcard.auth.domain.model.Role;
import com.shopcard.auth.domain.ports.out.RoleRepositoryPort;
import com.shopcard.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.shopcard.auth.infrastructure.persistence.mapper.PersistenceMapper;
import com.shopcard.auth.infrastructure.persistence.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Optional<Role> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity jpaEntity = mapper.toJpa(role);
        RoleJpaEntity saved = repository.save(jpaEntity);
        return mapper.toDomain(saved);
    }
}
