package com.shopcard.auth.security;

import java.util.Arrays;

public enum AppRole {
    CUSTOMER,
    SHOP_OWNER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    public static AppRole fromInput(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        String candidate = normalized;
        return Arrays.stream(values())
                .filter(role -> role.name().equals(candidate))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported role: " + value));
    }
}
