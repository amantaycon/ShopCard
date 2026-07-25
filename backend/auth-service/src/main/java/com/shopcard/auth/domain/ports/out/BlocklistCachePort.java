package com.shopcard.auth.domain.ports.out;

public interface BlocklistCachePort {
    void blocklistToken(String token, long remainingMillis);
}
