package nl.vintageforlife.poc.domain;

/**
 * Een bestelling die bezorgd moet worden. Komt overeen met de Order-class uit
 * het klassendiagram. Tijdvenster en service-flag worden door het algoritme
 * gebruikt als constraints (UR-11, UR-21).
 */
public class Order {
    private final String orderId;
    private final String customerId;
    private final Location address;
    /** Start tijdvenster in minuten sinds 00:00 (bijv. 9:00 = 540). */
    private final int timeWindowStart;
    /** Einde tijdvenster in minuten sinds 00:00. */
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

    /** Service tijd in minuten: 15 min normaal, 45 min met installatie (UR-21). */
    public int getServiceMinutes() {
        return requiresInstallation ? 45 : 15;
    }

    /** Helper: minuten sinds 00:00 -> HH:mm. */
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
