package nl.vintageforlife.poc.domain;

/**
 * Een bezorgvoertuig. Bevat id, laadcapaciteit (kg) en startlocatie (depot).
 * Komt overeen met de Vehicle-class uit het klassendiagram (FR-8 capaciteit).
 */
public class Vehicle {
    private final String vehicleId;
    /** Maximale laadcapaciteit in kilogram (FR-8, UR-20). */
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
