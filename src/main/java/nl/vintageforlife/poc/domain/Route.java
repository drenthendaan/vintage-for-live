package nl.vintageforlife.poc.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Het volledige bezorgplan voor een rit. Bevat voertuig, een driver,
 * een assistent (bijrijder), stops, verwachte starttijd en status.
 *
 * Bij Vintage for Life rijdt er altijd een tweetal mee: de driver bestuurt
 * het busje en de assistent helpt bij in- en uitladen / installatie.
 */
public class Route {

    public enum Status { CONCEPT, GOEDGEKEURD, IN_UITVOERING, AFGEROND }

    private final String routeId;
    private final Vehicle vehicle;
    private Deliverer driver;
    private Deliverer assistant;
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

    public Deliverer getDriver() { return driver; }
    public void setDriver(Deliverer driver) { this.driver = driver; }

    public Deliverer getAssistant() { return assistant; }
    public void setAssistant(Deliverer assistant) { this.assistant = assistant; }

    /** Compatibele helper: het tweetal als lijst (driver eerst, dan assistant). */
    public List<Deliverer> getCrew() {
        List<Deliverer> crew = new ArrayList<>();
        if (driver != null) crew.add(driver);
        if (assistant != null) crew.add(assistant);
        return crew;
    }

    public List<Stop> getStops() { return stops; }
    public int getPlannedStart() { return plannedStart; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double km) { this.totalDistanceKm = km; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public void addStop(Stop stop) { stops.add(stop); }

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
