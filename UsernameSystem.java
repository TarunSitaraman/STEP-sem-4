
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameSystem {

    private final Map<String, Integer> registeredUsers = new ConcurrentHashMap<>();
    private final Map<String, Integer> attemptTracker = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public boolean checkAvailability(String username) {
        attemptTracker.put(username, attemptTracker.getOrDefault(username, 0) + 1);
        return !registeredUsers.containsKey(username);
    }

    public void register(String username, int userId) {
        registeredUsers.put(username, userId);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        while (suggestions.size() < 3) {
            String candidate = username + (random.nextInt(900) + 100);
            if (!registeredUsers.containsKey(candidate)) {
                suggestions.add(candidate);
            }

            String simpleSuffix = username + suffix;
            if (!registeredUsers.containsKey(simpleSuffix) && !suggestions.contains(simpleSuffix)) {
                suggestions.add(simpleSuffix);
            }
            suffix++;
        }
        return suggestions;
    }

    public String getMostAttempted() {
        return attemptTracker.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No attempts recorded");
    }

    public static void main(String[] args) {
        UsernameSystem system = new UsernameSystem();

        system.register("john_doe", 101);

        System.out.println("john_doe available: " + system.checkAvailability("john_doe"));
        System.out.println("jane_smith available: " + system.checkAvailability("jane_smith"));

        if (!system.checkAvailability("john_doe")) {
            System.out.println("Suggestions for john_doe: " + system.suggestAlternatives("john_doe"));
        }

        System.out.println("Most attempted: " + system.getMostAttempted());
    }
}
