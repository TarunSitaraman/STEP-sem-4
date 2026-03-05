
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class VideoCacheSystem {

    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;
    private final int PROMOTION_THRESHOLD = 5;

    private final Map<String, String> l1Cache;
    private final Map<String, String> l2Cache;
    private final Map<String, Integer> accessCounts = new ConcurrentHashMap<>();

    private final AtomicLong l1Hits = new AtomicLong(0), l1Misses = new AtomicLong(0);
    private final AtomicLong l2Hits = new AtomicLong(0), l2Misses = new AtomicLong(0);
    private final AtomicLong l3Hits = new AtomicLong(0);

    public VideoCacheSystem() {
        this.l1Cache = Collections.synchronizedMap(new LinkedHashMap<String, String>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                if (size() > L1_CAPACITY) {
                    l2Cache.put(eldest.getKey(), eldest.getValue());
                    return true;
                }
                return false;
            }
        });

        this.l2Cache = Collections.synchronizedMap(new LinkedHashMap<String, String>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L2_CAPACITY;
            }
        });
    }

    public String getVideo(String videoId) {
        if (l1Cache.containsKey(videoId)) {
            l1Hits.incrementAndGet();
            return l1Cache.get(videoId);
        }
        l1Misses.incrementAndGet();

        if (l2Cache.containsKey(videoId)) {
            l2Hits.incrementAndGet();
            String data = l2Cache.get(videoId);
            trackAccessAndPromote(videoId, data);
            return data;
        }
        l2Misses.incrementAndGet();

        l3Hits.incrementAndGet();
        String data = fetchFromDatabase(videoId);
        l2Cache.put(videoId, data);
        accessCounts.put(videoId, 1);
        return data;
    }

    private void trackAccessAndPromote(String videoId, String data) {
        int count = accessCounts.getOrDefault(videoId, 0) + 1;
        accessCounts.put(videoId, count);

        if (count >= PROMOTION_THRESHOLD) {
            l2Cache.remove(videoId);
            l1Cache.put(videoId, data);
            accessCounts.remove(videoId);
        }
    }

    private String fetchFromDatabase(String videoId) {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
        }
        return "VideoContent_" + videoId;
    }

    public void getStatistics() {
        long total = l1Hits.get() + l1Misses.get();
        System.out.println("\n--- Cache Performance Report ---");
        System.out.printf("L1 Hit Rate: %.2f%%\n", (l1Hits.get() * 100.0 / total));
        System.out.printf("L2 Hit Rate: %.2f%%\n", (l2Hits.get() * 100.0 / l1Misses.get()));
        System.out.printf("L3 (DB) Accesses: %d\n", l3Hits.get());
    }

    public static void main(String[] args) {
        VideoCacheSystem netflix = new VideoCacheSystem();

        for (int i = 0; i < 6; i++) {
            System.out.println("Requesting video_123: " + netflix.getVideo("video_123"));
        }
        netflix.getStatistics();
    }
}
