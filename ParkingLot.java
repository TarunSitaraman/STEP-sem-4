
public class ParkingLot {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    class Spot {

        String licensePlate;
        long entryTime;
        Status status = Status.EMPTY;
    }

    private final int capacity = 503; // Prime numbers work better for quadratic probing
    private final Spot[] spots = new Spot[capacity];
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int parkActions = 0;

    public ParkingLot() {
        for (int i = 0; i < capacity; i++) {
            spots[i] = new Spot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public String parkVehicle(String licensePlate) {
        if (occupiedCount >= capacity) {
            return "Parking Lot Full";
        }

        int baseHash = hash(licensePlate);
        int currentSpot = baseHash;
        int i = 0;

        while (spots[currentSpot].status == Status.OCCUPIED) {
            i++;
            currentSpot = (baseHash + (i * i)) % capacity;
        }

        spots[currentSpot].licensePlate = licensePlate;
        spots[currentSpot].entryTime = System.currentTimeMillis();
        spots[currentSpot].status = Status.OCCUPIED;

        occupiedCount++;
        totalProbes += i;
        parkActions++;

        return String.format("Assigned spot #%d (%d probes)", currentSpot, i);
    }

    public String exitVehicle(String licensePlate) {
        int baseHash = hash(licensePlate);
        int currentSpot = baseHash;
        int i = 0;

        while (i < capacity) {
            if (spots[currentSpot].status == Status.EMPTY) {
                break;
            }

            if (spots[currentSpot].status == Status.OCCUPIED
                    && licensePlate.equals(spots[currentSpot].licensePlate)) {

                long durationMs = System.currentTimeMillis() - spots[currentSpot].entryTime;
                double fee = Math.max(1, durationMs / 3600000.0) * 5.0;

                spots[currentSpot].status = Status.DELETED;
                spots[currentSpot].licensePlate = null;
                occupiedCount--;

                return String.format("Spot #%d freed, Fee: $%.2f", currentSpot, fee);
            }
            i++;
            currentSpot = (baseHash + (i * i)) % capacity;
        }
        return "Vehicle not found";
    }

    public void getStatistics() {
        double occupancy = (occupiedCount * 100.0) / capacity;
        double avgProbes = parkActions == 0 ? 0 : (double) totalProbes / parkActions;
        System.out.printf("Occupancy: %.1f%%, Avg Probes: %.2f\n", occupancy, avgProbes);
    }

    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot();
        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));
        System.out.println(lot.exitVehicle("ABC-1234"));
        lot.getStatistics();
    }
}
