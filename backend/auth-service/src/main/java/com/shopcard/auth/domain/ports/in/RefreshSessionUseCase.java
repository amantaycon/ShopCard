package com.shopcard.auth.domain.ports.in;

public interface RefreshSessionUseCase {
    AuthResult refresh(String refreshToken);
}
