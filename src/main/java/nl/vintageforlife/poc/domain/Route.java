package nl.vintageforlife.poc.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Het volledige bezorgplan voor een rit. Bevat voertuig, bezorger(s), stops,
 * verwachte starttijd en status.
 */
public class Route {

    public enum Status { CONCEPT, GOEDGEKEURD, IN_UITVOERING, AFGEROND }

    private final String routeId;
    private final Vehicle vehicle;
    private final List<Deliverer> deliverers = new ArrayList<>();
    private final List<Stop> stops = new ArrayList<>();
    /** Geplande starttijd in minuten sinds 00:00. */
    private int plannedStart;
    private double totalDistanceKm;
    private Status status = Status.CONCEPT;

    public Route(String routeId, Vehicle vehicle, int plannedStart) {
        this.routeId = routeId;
        this.vehicle = vehicle;
        this.plannedStart = plannedStart;
    }

    public String getRouteId() { return routeId; }
    public Vehicle getVehicle() { return vehicle; }
    public List<Deliverer> getDeliverers() { return deliverers; }
    public List<Stop> getStops() { return stops; }
    public int getPlannedStart() { return plannedStart; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double km) { this.totalDistanceKm = km; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public void addStop(Stop stop) { stops.add(stop); }
    public void addDeliverer(Deliverer d) { deliverers.add(d); }

    public int totalWeightKg() {
        return stops.stream().mapToInt(s -> s.getOrder().getWeightKg()).sum();
    }

    public String formattedStart() {
        return String.format("%02d:%02d", plannedStart / 60, plannedStart % 60);
    }

    @Override
    public String toString() {
        return routeId + " (" + vehicle.getVehicleId() + ", " + stops.size() + " stops)";
    }
}
