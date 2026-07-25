package com.shopcard.auth.infrastructure.cache.adapter;

import com.shopcard.auth.domain.ports.out.EmailVerificationCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailVerificationCacheAdapter implements EmailVerificationCachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void storeCode(String email, String code) {
        redisTemplate.opsForValue().set("email_verify_code:" + email, code, Duration.ofMinutes(15));
    }

    @Override
    public String getCode(String email) {
        return redisTemplate.opsForValue().get("email_verify_code:" + email);
    }

    @Override
    public void clearCode(String email) {
        redisTemplate.delete("email_verify_code:" + email);
    }

    @Override
    public void markEmailAsVerified(String email) {
        redisTemplate.opsForValue().set("email_verified_status:" + email, "VERIFIED", Duration.ofMinutes(30));
    }

    @Override
    public boolean isEmailVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("email_verified_status:" + email));
    }

    @Override
    public void clearEmailVerificationStatus(String email) {
        redisTemplate.delete("email_verified_status:" + email);
    }
}
