package com.waste.helper.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    // Guest: 30 requests per day
    public static final int GUEST_DAILY_LIMIT = 30;
    // User: 100 requests per day
    public static final int USER_DAILY_LIMIT = 100;
    // IP: 10 requests per minute
    public static final int IP_MINUTE_LIMIT = 10;

    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    public static Bucket createGuestBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(GUEST_DAILY_LIMIT, Refill.intervally(GUEST_DAILY_LIMIT, Duration.ofDays(1))))
            .build();
    }

    public static Bucket createUserBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(USER_DAILY_LIMIT, Refill.intervally(USER_DAILY_LIMIT, Duration.ofDays(1))))
            .build();
    }

    public static Bucket createIpBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(IP_MINUTE_LIMIT, Refill.intervally(IP_MINUTE_LIMIT, Duration.ofMinutes(1))))
            .build();
    }
}
