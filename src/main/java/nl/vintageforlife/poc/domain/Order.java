package nl.vintageforlife.poc.domain;

/**
 * An order that needs to be delivered. Matches the Order class from the
 * class diagram. The time window and installation flag are used by the
 * algorithm as constraints (UR-11, UR-21).
 */
public class Order {
    private final String orderId;
    private final String customerId;
    private final Location address;
    /** Start of the time window in minutes since 00:00 (e.g. 9:00 = 540). */
    private final int timeWindowStart;
    /** End of the time window in minutes since 00:00. */
    private final int timeWindowEnd;
    private final int weightKg;
    private final boolean requiresInstallation;
    private boolean delivered;

    public Order(String orderId, String customerId, Location address,
                 int timeWindowStart, int timeWindowEnd,
                 int weightKg, boolean requiresInstallation) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.address = address;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.weightKg = weightKg;
        this.requiresInstallation = requiresInstallation;
        this.delivered = false;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public Location getAddress() { return address; }
    public int getTimeWindowStart() { return timeWindowStart; }
    public int getTimeWindowEnd() { return timeWindowEnd; }
    public int getWeightKg() { return weightKg; }
    public boolean requiresInstallation() { return requiresInstallation; }
    public boolean isDelivered() { return delivered; }
    public void markDelivered() { this.delivered = true; }

    /** Service time in minutes: 15 min standard, 45 min with installation (UR-21). */
    public int getServiceMinutes() {
        return requiresInstallation ? 45 : 15;
    }

    /** Helper: minutes since 00:00 -> HH:mm. */
    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    @Override
    public String toString() {
        return orderId + " | " + address + " | "
             + formatTime(timeWindowStart) + "-" + formatTime(timeWindowEnd)
             + " | " + weightKg + " kg"
             + (requiresInstallation ? " | installatie" : "");
    }
}
