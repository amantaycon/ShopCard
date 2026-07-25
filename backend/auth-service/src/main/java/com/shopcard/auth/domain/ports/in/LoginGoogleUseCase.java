package com.shopcard.auth.domain.ports.in;

public interface LoginGoogleUseCase {
    AuthResult loginGoogle(GoogleLoginCommand command);
}
