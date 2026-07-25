package com.shopcard.auth.infrastructure.persistence.mapper;

import com.shopcard.auth.domain.model.PasswordResetToken;
import com.shopcard.auth.domain.model.Role;
import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.model.VerificationToken;
import com.shopcard.auth.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.shopcard.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.shopcard.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.shopcard.auth.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PersistenceMapper {

    public Role toDomain(RoleJpaEntity entity) {
        if (entity == null) return null;
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public RoleJpaEntity toJpa(Role domain) {
        if (domain == null) return null;
        return RoleJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .isActive(entity.getIsActive())
                .emailVerified(entity.getEmailVerified())
                .roles(entity.getRoles().stream().map(this::toDomain).collect(Collectors.toSet()))
                .username(entity.getUsername())
                .dateOfBirth(entity.getDateOfBirth())
                .agreedTerms(entity.getAgreedTerms())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
     }
 
     public UserJpaEntity toJpa(User domain) {
         if (domain == null) return null;
         return UserJpaEntity.builder()
                 .id(domain.getId())
                 .email(domain.getEmail())
                 .passwordHash(domain.getPasswordHash())
                 .firstName(domain.getFirstName())
                 .lastName(domain.getLastName())
                 .phoneNumber(domain.getPhoneNumber())
                 .provider(domain.getProvider())
                 .providerId(domain.getProviderId())
                 .isActive(domain.getIsActive())
                 .emailVerified(domain.getEmailVerified())
                 .roles(domain.getRoles().stream().map(this::toJpa).collect(Collectors.toSet()))
                 .username(domain.getUsername())
                 .dateOfBirth(domain.getDateOfBirth())
                 .agreedTerms(domain.getAgreedTerms())
                 .createdAt(domain.getCreatedAt())
                 .updatedAt(domain.getUpdatedAt())
                 .build();
     }

    public VerificationToken toDomain(VerificationTokenJpaEntity entity) {
        if (entity == null) return null;
        return VerificationToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .user(toDomain(entity.getUser()))
                .expiryDate(entity.getExpiryDate())
                .build();
    }

    public VerificationTokenJpaEntity toJpa(VerificationToken domain) {
        if (domain == null) return null;
        return VerificationTokenJpaEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .user(toJpa(domain.getUser()))
                .expiryDate(domain.getExpiryDate())
                .build();
    }

    public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        if (entity == null) return null;
        return PasswordResetToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .user(toDomain(entity.getUser()))
                .expiryDate(entity.getExpiryDate())
                .build();
    }

    public PasswordResetTokenJpaEntity toJpa(PasswordResetToken domain) {
        if (domain == null) return null;
        return PasswordResetTokenJpaEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .user(toJpa(domain.getUser()))
                .expiryDate(domain.getExpiryDate())
                .build();
    }
}
