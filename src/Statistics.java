import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Statistics {
    private final AtomicInteger clientCount = new AtomicInteger();
    private final AtomicInteger requestCount = new AtomicInteger();
    private final ConcurrentHashMap<String, AtomicInteger> operations = new ConcurrentHashMap<>();
    private final AtomicInteger invalidOps = new AtomicInteger();
    private final AtomicInteger sum = new AtomicInteger();

    public void incrementClientCount() { clientCount.incrementAndGet(); }
    public void incrementRequestCount() { requestCount.incrementAndGet(); }
    public void incrementOperation(String operation) {
        operations.computeIfAbsent(operation, k -> new AtomicInteger()).incrementAndGet();
    }
    public void incrementInvalidOps() { invalidOps.incrementAndGet(); }
    public void addToSum(int value) { sum.addAndGet(value); }

    public void printStatistics() {
        System.out.println("=== Statistics ===");
        System.out.println("Clients connected: " + clientCount.get());
        System.out.println("Requests processed: " + requestCount.get());
        System.out.println("Invalid operations: " + invalidOps.get());
        System.out.println("Sum of results: " + sum.get());
        System.out.println("Operation counts: " + operations);
    }
}
