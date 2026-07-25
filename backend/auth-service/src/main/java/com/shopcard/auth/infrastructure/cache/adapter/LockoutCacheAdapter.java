package com.shopcard.auth.infrastructure.cache.adapter;

import com.shopcard.auth.domain.ports.out.LockoutCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LockoutCacheAdapter implements LockoutCachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isLocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("lockout:" + email));
    }

    @Override
    public void incrementFailedAttempts(String email) {
        String countKey = "failed_login:" + email;
        Long attempts = redisTemplate.opsForValue().increment(countKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(countKey, Duration.ofMinutes(15));
        }
        if (attempts != null && attempts >= 5) {
            redisTemplate.opsForValue().set("lockout:" + email, "LOCKED", Duration.ofMinutes(15));
            redisTemplate.delete(countKey);
        }
    }

    @Override
    public void clearFailedAttempts(String email) {
        redisTemplate.delete("failed_login:" + email);
    }
}
