package com.shopcard.auth.domain.ports.in;

import java.util.UUID;

public interface UpdateRoleUseCase {
    void updateRole(UUID userId, String role);
}
