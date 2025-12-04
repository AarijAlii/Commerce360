package Commerce360.service;

import Commerce360.config.RateLimiterConfig;
import Commerce360.exception.RateLimitExceededException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RateLimiterService {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Counter allowedRequests;
    private final Counter rejectedRequests;
    private final Counter totalBuckets;
    private final RateLimiterConfig config;
    private final long leakInterval;

    public RateLimiterService(MeterRegistry registry, RateLimiterConfig config) {
        this.config = config;
        this.leakInterval = TimeUnit.SECONDS.toMillis(1) / config.getLeakRate();

        // Initialize metrics
        this.allowedRequests = Counter.builder("rate_limiter.requests.allowed")
                .description("Number of allowed requests")
                .register(registry);

        this.rejectedRequests = Counter.builder("rate_limiter.requests.rejected")
                .description("Number of rejected requests")
                .register(registry);

        this.totalBuckets = Counter.builder("rate_limiter.buckets.total")
                .description("Total number of active buckets")
                .register(registry);
    }

    private static class Bucket {
        private int tokens;
        private long lastLeakTime;
        private long lastAccessTime;
        private final ReentrantLock lock = new ReentrantLock();

        Bucket(int capacity) {
            this.tokens = capacity;
            this.lastLeakTime = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    public boolean tryAcquire(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }

        try {
            Bucket bucket = buckets.computeIfAbsent(clientId, k -> {
                logger.info("Creating new bucket for client: {}", clientId);
                totalBuckets.increment();
                return new Bucket(config.getCapacity());
            });

            bucket.lock.lock();
            try {
                bucket.lastAccessTime = System.currentTimeMillis();
                long now = System.currentTimeMillis();
                long timePassed = now - bucket.lastLeakTime;
                int leaks = (int) (timePassed / leakInterval);

                if (leaks > 0) {
                    int oldTokens = bucket.tokens;
                    bucket.tokens = Math.min(config.getCapacity(), bucket.tokens + leaks);
                    bucket.lastLeakTime = now;
                    logger.debug("Bucket leaked {} tokens for client {}. Old tokens: {}, New tokens: {}",
                            leaks, clientId, oldTokens, bucket.tokens);
                }

                if (bucket.tokens > 0) {
                    bucket.tokens--;
                    allowedRequests.increment();
                    logger.debug("Request allowed for client {}. Remaining tokens: {}", clientId, bucket.tokens);
                    return true;
                }

                rejectedRequests.increment();
                logger.warn("Rate limit exceeded for client {}. Request rejected.", clientId);
                throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
            } finally {
                bucket.lock.unlock();
            }
        } catch (Exception e) {
            logger.error("Error processing rate limit for client {}: {}", clientId, e.getMessage());
            throw e;
        }
    }

    @Scheduled(fixedRateString = "#{@rateLimiterConfig.cleanupInterval}")
    public void cleanupInactiveBuckets() {
        try {
            long now = System.currentTimeMillis();
            AtomicInteger removedCount = new AtomicInteger(0);
            buckets.entrySet().removeIf(entry -> {
                Bucket bucket = entry.getValue();
                bucket.lock.lock();
                try {
                    boolean shouldRemove = (now - bucket.lastAccessTime) > config.getCleanupInterval();
                    if (shouldRemove) {
                        removedCount.incrementAndGet();
                    }
                    return shouldRemove;
                } finally {
                    bucket.lock.unlock();
                }
            });
            totalBuckets.increment(-removedCount.get());
            logger.info("Cleaned up {} inactive buckets. Current bucket count: {}", removedCount.get(), buckets.size());
        } catch (Exception e) {
            logger.error("Error during bucket cleanup: {}", e.getMessage());
        }
    }
}