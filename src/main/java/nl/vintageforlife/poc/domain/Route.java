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

    /** Dutch day formatter, e.g. "vr 15 mei 2026". */
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMMM yyyy", new Locale("nl"));

    private final String routeId;
    private final Vehicle vehicle;
    private Deliverer driver;
    private Deliverer assistant;
    private final List<Stop> stops = new ArrayList<>();
    /** Planned start time in minutes since 00:00. */
    private int plannedStart;
    /** Day offset relative to today (0 = today, 1 = tomorrow, ...). */
    private int dayOffset;
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

    /** Compatibility helper: the pair as a list (driver first, then assistant). */
    public List<Deliverer> getCrew() {
        List<Deliverer> crew = new ArrayList<>();
        if (driver != null) crew.add(driver);
        if (assistant != null) crew.add(assistant);
        return crew;
    }

    public List<Stop> getStops() { return stops; }
    public int getPlannedStart() { return plannedStart; }
    public int getDayOffset() { return dayOffset; }
    public void setDayOffset(int dayOffset) { this.dayOffset = dayOffset; }
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

    /** Planned date for this route, relative to today's date. */
    public LocalDate getPlannedDate() {
        return LocalDate.now().plusDays(dayOffset);
    }

    /** Human-readable date label, e.g. "vr 15 mei 2026". */
    public String formattedDay() {
        return DAY_FORMAT.format(getPlannedDate());
    }

    @Override
    public String toString() {
        return routeId + " (" + vehicle.getVehicleId() + ", " + stops.size() + " stops)";
    }
}
