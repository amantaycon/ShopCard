package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    User save(User user);
}
