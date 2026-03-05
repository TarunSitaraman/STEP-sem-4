
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DNSCache {

    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    class DNSEntry {

        String ip;
        long expiryTime;

        DNSEntry(String ip, long ttlSeconds) {
            this.ip = ip;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public DNSCache(int capacity) {
        this.capacity = capacity;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        });
    }

    public String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            return entry.ip;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses.incrementAndGet();
        String ip = queryUpstream(domain);
        cache.put(domain, new DNSEntry(ip, 300));
        return ip;
    }

    private String queryUpstream(String domain) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        return "172.217.14." + (new Random().nextInt(254) + 1);
    }

    public void getCacheStats() {
        long total = hits.get() + misses.get();
        double hitRate = (total == 0) ? 0 : (double) hits.get() / total * 100;
        System.out.printf("Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.2f%%\n",
                hits.get(), misses.get(), hitRate);
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache(2);

        System.out.println("Resolving google.com: " + dns.resolve("google.com"));
        System.out.println("Resolving google.com (Cached): " + dns.resolve("google.com"));

        dns.resolve("example.com");
        dns.resolve("openai.com");

        dns.getCacheStats();
    }
}
