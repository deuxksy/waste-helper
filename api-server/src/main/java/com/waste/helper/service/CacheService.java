package com.waste.helper.service;

import com.waste.helper.service.dto.ClassifyDetailResponse;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CLASSIFY_CACHE_PREFIX = "classify:";
    private static final long CACHE_TTL_HOURS = 24;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ClassifyDetailResponse getCachedClassification(String cacheKey) {
        return (ClassifyDetailResponse) redisTemplate.opsForValue().get(CLASSIFY_CACHE_PREFIX + cacheKey);
    }

    public void cacheClassification(String cacheKey, ClassifyDetailResponse response) {
        redisTemplate.opsForValue().set(CLASSIFY_CACHE_PREFIX + cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    public String generateCacheKey(String detectedClass, String regionCode) {
        return detectedClass + ":" + (regionCode != null ? regionCode : "default");
    }
}
