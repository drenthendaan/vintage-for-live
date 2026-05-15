package nl.vintageforlife.poc.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The complete delivery plan for a trip. Contains the vehicle, a driver,
 * an assistant, stops, expected start time and status.
 *
 * At Vintage for Life every delivery is done by a pair: the driver operates
 * the van and the assistant helps with loading/unloading and installation.
 */
public class Route {

    public enum Status { CONCEPT, GOEDGEKEURD, IN_UITVOERING, AFGEROND }

    private final String routeId;
    private final Vehicle vehicle;
    private Deliverer driver;
    private Deliverer assistant;
    private final List<Stop> stops = new ArrayList<>();
    /** Planned start time in minutes since 00:00. */
    private int plannedStart;
    /** Planned date on which this route should be driven. */
    private LocalDate plannedDate = LocalDate.now();
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

    public List<Stop> getStops() { return stops; }
    public int getPlannedStart() { return plannedStart; }
    public void setPlannedStart(int plannedStart) { this.plannedStart = plannedStart; };
    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double km) { this.totalDistanceKm = km; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public void addStop(Stop stop) { stops.add(stop); }

    public int totalWeightKg() {
        return stops.stream().mapToInt(stop -> stop.getOrder().getWeightKg()).sum();
    }

    public String formattedStart() {
        return String.format("%02d:%02d", plannedStart / 60, plannedStart % 60);
    }

    /** Human-readable date label, e.g. "vr 15 mei 2026". */
    public String formattedDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE d MMMM yyyy", new Locale("nl"));
        return formatter.format(plannedDate);
    }

    @Override
    public String toString() {
        return routeId + " (" + vehicle.getVehicleId() + ", " + stops.size() + " stops)";
    }
}
