package com.shopcard.auth.domain.ports.in;

import java.time.LocalDate;
import java.util.UUID;

public interface UpdateProfileUseCase {
    void updateProfile(UUID userId, String firstName, String lastName, LocalDate dateOfBirth, String username, Boolean agreedTerms);
}
