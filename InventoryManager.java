
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryManager {

    private final Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();
    private final Map<String, Queue<Integer>> waitingLists = new ConcurrentHashMap<>();

    public void addProduct(String productId, int initialStock) {
        stockMap.put(productId, new AtomicInteger(initialStock));
        waitingLists.put(productId, new ConcurrentLinkedQueue<>());
    }

    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();
            if (currentStock <= 0) {
                Queue<Integer> waitList = waitingLists.get(productId);
                waitList.add(userId);
                List<Integer> list = new ArrayList<>(waitList);
                return "Added to waiting list, position #" + (list.indexOf(userId) + 1);
            }

            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        manager.addProduct("IPHONE15_256GB", 2);

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 88888));
    }
}
