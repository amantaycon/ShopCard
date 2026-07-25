package com.shopcard.auth.domain.ports.in;

public interface LoginUseCase {
    AuthResult login(LoginCommand command);
}
