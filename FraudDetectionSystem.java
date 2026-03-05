
import java.util.*;

public class FraudDetectionSystem {

    record Transaction(int id, int amount, String merchant, long timestamp, String account) {

    }

    public List<List<Integer>> findTwoSum(List<Transaction> transactions, int target) {
        List<List<Integer>> pairs = new ArrayList<>();
        Map<Integer, Integer> seen = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount();
            if (seen.containsKey(complement)) {
                pairs.add(List.of(seen.get(complement), t.id()));
            }
            seen.put(t.amount(), t.id());
        }
        return pairs;
    }

    public List<List<Integer>> findTwoSumWithTimeWindow(List<Transaction> transactions, int target, long windowMs) {
        List<List<Integer>> pairs = new ArrayList<>();
        Map<Integer, Transaction> seen = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount();
            if (seen.containsKey(complement)) {
                Transaction partner = seen.get(complement);
                if (Math.abs(t.timestamp() - partner.timestamp()) <= windowMs) {
                    pairs.add(List.of(partner.id(), t.id()));
                }
            }
            seen.put(t.amount(), t);
        }
        return pairs;
    }

    public List<Map<String, Object>> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<Transaction>> merchantAmountMap = new HashMap<>();
        List<Map<String, Object>> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.merchant() + "_" + t.amount();
            merchantAmountMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : merchantAmountMap.entrySet()) {
            List<Transaction> list = entry.getValue();
            if (list.size() > 1) {
                Set<String> accounts = new HashSet<>();
                for (Transaction t : list) {
                    accounts.add(t.account());
                }

                if (accounts.size() > 1) {
                    Map<String, Object> dup = new HashMap<>();
                    String[] parts = entry.getKey().split("_");
                    dup.put("merchant", parts[0]);
                    dup.put("amount", Integer.parseInt(parts[1]));
                    dup.put("accounts", accounts);
                    duplicates.add(dup);
                }
            }
        }
        return duplicates;
    }

    public static void main(String[] args) {
        FraudDetectionSystem system = new FraudDetectionSystem();
        List<Transaction> txns = List.of(
                new Transaction(1, 500, "Store A", 1000, "Acc1"),
                new Transaction(2, 300, "Store B", 2000, "Acc2"),
                new Transaction(3, 200, "Store C", 3000, "Acc3"),
                new Transaction(4, 500, "Store A", 4000, "Acc4")
        );

        System.out.println("Two Sum (500): " + system.findTwoSum(txns, 500));
        System.out.println("Duplicates: " + system.detectDuplicates(txns));
    }
}
