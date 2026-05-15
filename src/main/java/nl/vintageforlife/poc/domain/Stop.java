package nl.vintageforlife.poc.domain;

/**
 * Een aflevermoment binnen een route. Heeft een volgnummer en is gekoppeld
 * aan exact één Order (zie klassendiagram).
 */
public class Stop {
    private final String stopId;
    private final int sequence;
    private final Order order;
    /** Verwachte aankomsttijd in minuten sinds 00:00. */
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

    /** Markeer de stop als afgerond; markeert tegelijk de bijbehorende order als bezorgd. */
    public void markCompleted() {
        this.completed = true;
        this.order.markDelivered();
    }

    /** Helper: ETA als leesbare HH:mm string. */
    public String formattedEta() {
        return String.format("%02d:%02d", eta / 60, eta % 60);
    }
}
