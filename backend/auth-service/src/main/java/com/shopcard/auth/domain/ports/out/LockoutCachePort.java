package com.shopcard.auth.domain.ports.out;

public interface LockoutCachePort {
    boolean isLocked(String email);
    void incrementFailedAttempts(String email);
    void clearFailedAttempts(String email);
}
