package com.shopcard.auth.domain.ports.in;

import java.util.Set;

public record RegisterCommand(
    String email,
    String password,
    String firstName,
    String lastName,
    String phoneNumber,
    Set<String> roles
) {}
