package com.shopcard.auth.domain.ports.out;

public interface EmailVerificationCachePort {
    void storeCode(String email, String code);
    String getCode(String email);
    void clearCode(String email);
    void markEmailAsVerified(String email);
    boolean isEmailVerified(String email);
    void clearEmailVerificationStatus(String email);
}
