package com.shopcard.profile.infrastructure.persistence.repository;

import com.shopcard.profile.domain.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUsername(String username);
    boolean existsByUsername(String username);
}
