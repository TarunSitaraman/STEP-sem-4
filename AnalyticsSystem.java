
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class AnalyticsSystem {

    private final Map<String, LongAdder> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> trafficSources = new ConcurrentHashMap<>();
    private final LongAdder totalEvents = new LongAdder();

    public void processEvent(String url, String userId, String source) {
        pageViews.computeIfAbsent(url, k -> new LongAdder()).increment();

        trafficSources.computeIfAbsent(source, k -> new LongAdder()).increment();

        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);

        totalEvents.increment();
    }

    public void getDashboard() {
        System.out.println("\n--- Real-Time Dashboard (Last 5s) ---");

        System.out.println("Top Pages:");
        pageViews.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().sum(), e1.getValue().sum()))
                .limit(10)
                .forEach(entry -> {
                    String url = entry.getKey();
                    long views = entry.getValue().sum();
                    int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
                    System.out.printf("%s - %d views (%d unique)\n", url, views, uniques);
                });

        System.out.println("\nTraffic Sources:");
        long total = totalEvents.sum();
        trafficSources.forEach((source, count) -> {
            double percent = (count.sum() * 100.0) / total;
            System.out.printf("%s: %.1f%%\n", source, percent);
        });
    }

    public static void main(String[] args) throws InterruptedException {
        AnalyticsSystem analytics = new AnalyticsSystem();

        analytics.processEvent("/article/breaking-news", "user_123", "Google");
        analytics.processEvent("/article/breaking-news", "user_456", "Facebook");
        analytics.processEvent("/article/breaking-news", "user_123", "Google");
        analytics.processEvent("/sports/championship", "user_789", "Direct");

        analytics.getDashboard();
    }
}
