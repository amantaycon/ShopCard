package com.shopcard.auth.domain.ports.in;

public interface LogoutUseCase {
    void logout(String authHeader);
}
