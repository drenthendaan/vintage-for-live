package nl.vintageforlife.poc.domain;

/**
 * A single delivery moment within a route. Has a sequence number and is
 * linked to exactly one Order (see class diagram).
 */
public class Stop {
    private final String stopId;
    private final int sequence;
    private final Order order;
    /** Expected time of arrival in minutes since 00:00. */
    private int eta;
    private boolean completed;

    public Stop(String stopId, int sequence, Order order, int eta) {
        this.stopId = stopId;
        this.sequence = sequence;
        this.order = order;
        this.eta = eta;
        this.completed = false;
    }

    public String getStopId() { return stopId; }
    public int getSequence() { return sequence; }
    public Order getOrder() { return order; }
    public int getEta() { return eta; }
    public void setEta(int eta) { this.eta = eta; }
    public boolean isCompleted() { return completed; }

    /** Marks the stop as completed and also marks the underlying order as delivered. */
    public void markCompleted() {
        this.completed = true;
        this.order.markDelivered();
    }

    /** Helper: ETA formatted as a readable HH:mm string. */
    public String formattedEta() {
        return String.format("%02d:%02d", eta / 60, eta % 60);
    }
}
