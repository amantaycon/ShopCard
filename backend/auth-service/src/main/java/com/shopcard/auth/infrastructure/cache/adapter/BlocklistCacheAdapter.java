package com.shopcard.auth.infrastructure.cache.adapter;

import com.shopcard.auth.domain.ports.out.BlocklistCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BlocklistCacheAdapter implements BlocklistCachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blocklistToken(String token, long remainingMillis) {
        redisTemplate.opsForValue().set("blocklist:" + token, "LOGGED_OUT", Duration.ofMillis(remainingMillis));
    }
}
