package nl.vintageforlife.poc.domain;

/**
 * A delivery vehicle. Holds id, load capacity (kg) and start location
 * (depot). Matches the Vehicle class from the class diagram (FR-8 capacity).
 */
public class Vehicle {
    private final String vehicleId;
    /** Maximum load capacity in kilograms (FR-8, UR-20). */
    private final int capacityKg;
    private final Location startLocation;

    public Vehicle(String vehicleId, int capacityKg, Location startLocation) {
        this.vehicleId = vehicleId;
        this.capacityKg = capacityKg;
        this.startLocation = startLocation;
    }

    public String getVehicleId() { return vehicleId; }
    public int getCapacityKg() { return capacityKg; }
    public Location getStartLocation() { return startLocation; }

    @Override
    public String toString() {
        return vehicleId + " (" + capacityKg + " kg)";
    }
}
