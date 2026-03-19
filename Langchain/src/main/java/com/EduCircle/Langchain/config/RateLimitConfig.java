package com.EduCircle.Langchain.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    @Value("${rate-limit.search-per-hour:50}")
    private int searchPerHour;

    @Value("${rate-limit.ai-per-hour:100}")
    private int aiPerHour;

    @Value("${rate-limit.auth-per-minute:10}")
    private int authPerMinute;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket getSearchBucket(String userId) {
        return buckets.computeIfAbsent("search:" + userId, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(searchPerHour,
                                Refill.intervally(searchPerHour, Duration.ofHours(1))))
                        .build()
        );
    }

    public Bucket getAiBucket(String userId) {
        return buckets.computeIfAbsent("ai:" + userId, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(aiPerHour,
                                Refill.intervally(aiPerHour, Duration.ofHours(1))))
                        .build()
        );
    }

    public Bucket getAuthBucket(String ip) {
        return buckets.computeIfAbsent("auth:" + ip, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(authPerMinute,
                                Refill.intervally(authPerMinute, Duration.ofMinutes(1))))
                        .build()
        );
    }
}