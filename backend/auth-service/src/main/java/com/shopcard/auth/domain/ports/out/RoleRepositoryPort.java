package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.Role;

import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
    Role save(Role role);
}
