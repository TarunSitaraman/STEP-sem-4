
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    private final Map<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private final long maxTokens = 1000;
    private final long refillIntervalMs = 3600000; // 1 hour

    class TokenBucket {

        private final AtomicLong tokens;
        private volatile long lastRefillTime;

        TokenBucket() {
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now > lastRefillTime + refillIntervalMs) {
                tokens.set(maxTokens);
                lastRefillTime = now;
            }
        }

        long getRemaining() {
            refill();
            return tokens.get();
        }

        long getSecondsUntilReset() {
            return (lastRefillTime + refillIntervalMs - System.currentTimeMillis()) / 1000;
        }
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId, k -> new TokenBucket());

        if (bucket.tryConsume()) {
            return "Allowed (" + bucket.getRemaining() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after " + bucket.getSecondsUntilReset() + "s)";
        }
    }

    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();
        String client = "abc123";

        for (int i = 0; i < 3; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }
    }
}
