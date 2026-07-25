package com.shopcard.catalog.infrastructure.persistence.adapter;

import com.shopcard.catalog.domain.model.ImportJob;
import com.shopcard.catalog.domain.ports.out.ImportJobRepositoryPort;
import com.shopcard.catalog.infrastructure.persistence.PersistenceMapper;
import com.shopcard.catalog.infrastructure.persistence.entity.ImportJobJpaEntity;
import com.shopcard.catalog.infrastructure.persistence.repository.ImportJobJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImportJobRepositoryAdapter implements ImportJobRepositoryPort {

    private final ImportJobJpaRepository importJobJpaRepository;

    @Override
    public ImportJob save(ImportJob job) {
        ImportJobJpaEntity jpaEntity = PersistenceMapper.toJpa(job);
        ImportJobJpaEntity savedEntity = importJobJpaRepository.save(jpaEntity);
        return PersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ImportJob> findById(UUID id) {
        return importJobJpaRepository.findById(id)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<ImportJob> findByShopId(UUID shopId) {
        return importJobJpaRepository.findByShopId(shopId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
