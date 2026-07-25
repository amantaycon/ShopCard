package com.shopcard.auth.infrastructure.persistence.repository;

import com.shopcard.auth.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenJpaRepository extends JpaRepository<VerificationTokenJpaEntity, UUID> {
    Optional<VerificationTokenJpaEntity> findByToken(String token);
}
