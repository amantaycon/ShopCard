package com.shopcard.auth.domain.ports.in;

public interface RegisterUserUseCase {
    AuthResult register(RegisterCommand command);
}
