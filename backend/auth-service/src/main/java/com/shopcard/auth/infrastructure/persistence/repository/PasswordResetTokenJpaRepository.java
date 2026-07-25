package com.shopcard.auth.infrastructure.persistence.repository;

import com.shopcard.auth.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.shopcard.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    Optional<PasswordResetTokenJpaEntity> findByToken(String token);
    Optional<PasswordResetTokenJpaEntity> findByUser(UserJpaEntity user);
}
