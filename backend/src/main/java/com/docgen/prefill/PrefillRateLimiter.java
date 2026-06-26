package com.docgen.prefill;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Token-bucket rate limiter for prefill calls. Uses Redis when reachable (shared across
 * instances), otherwise transparently falls back to an in-memory limiter.
 */
@Component
public class PrefillRateLimiter {

    private final StringRedisTemplate redis;
    private final int capacity;
    private final int refillPerMinute;
    private final ConcurrentHashMap<String, Window> local = new ConcurrentHashMap<>();
    private volatile boolean redisHealthy;

    private record Window(AtomicLong count, AtomicLong windowStartMs) {}

    public PrefillRateLimiter(
            org.springframework.beans.factory.ObjectProvider<StringRedisTemplate> redisProvider,
            @Value("${docgen.prefill.rate-limit.capacity}") int capacity,
            @Value("${docgen.prefill.rate-limit.refill-per-minute}") int refillPerMinute) {
        this.redis = redisProvider.getIfAvailable();
        this.capacity = capacity;
        this.refillPerMinute = refillPerMinute;
        this.redisHealthy = this.redis != null;
    }

    /** @return true if the call is allowed, false if the limit is exceeded. */
    public boolean tryAcquire(String key) {
        if (redisHealthy) {
            try {
                return tryAcquireRedis(key);
            } catch (Exception e) {
                // Redis became unreachable — fall back to in-memory for the rest of this run.
                redisHealthy = false;
            }
        }
        return tryAcquireLocal(key);
    }

    private boolean tryAcquireRedis(String key) {
        String redisKey = "prefill:rl:" + key;
        Long count = redis.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redis.expire(redisKey, Duration.ofMinutes(1));
        }
        return count != null && count <= Math.max(capacity, refillPerMinute);
    }

    private boolean tryAcquireLocal(String key) {
        long now = System.currentTimeMillis();
        Window w = local.computeIfAbsent(key, k -> new Window(new AtomicLong(0), new AtomicLong(now)));
        synchronized (w) {
            if (now - w.windowStartMs().get() >= 60_000) {
                w.windowStartMs().set(now);
                w.count().set(0);
            }
            return w.count().incrementAndGet() <= Math.max(capacity, refillPerMinute);
        }
    }

    public boolean usingRedis() {
        return redisHealthy;
    }
}
