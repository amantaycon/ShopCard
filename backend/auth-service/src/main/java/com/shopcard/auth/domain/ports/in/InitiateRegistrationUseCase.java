package com.shopcard.auth.domain.ports.in;

public interface InitiateRegistrationUseCase {
    AuthResult initiateRegistration(String email, String password);
}
