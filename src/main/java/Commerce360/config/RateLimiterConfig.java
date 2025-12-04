package Commerce360.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "rate.limiter")
@Validated
public class RateLimiterConfig {
    @Min(1)
    private int capacity = 10;

    @Min(1)
    private int leakRate = 1;

    @Min(1000)
    private long cleanupInterval = 300000;

    // Getters and setters
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLeakRate() {
        return leakRate;
    }

    public void setLeakRate(int leakRate) {
        this.leakRate = leakRate;
    }

    public long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
}