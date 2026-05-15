package nl.vintageforlife.poc.domain;

/**
 * Simple lat/lon location. Used for the depot, the vehicle start point and
 * order addresses. In the PoC we compute distance as the crow flies
 * (haversine); in production GraphHopper would provide the real driving
 * distances (see the technical design, chapter "Chosen algorithm").
 */
public class Location {
    private final String address;
    private final double lat;
    private final double lon;

    public Location(String address, double lat, double lon) {
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }

    /** Haversine distance in kilometers between this location and {@code other}. */
    public double distanceKm(Location other) {
        final double R = 6371.0;
        double dLat = Math.toRadians(other.lat - this.lat);
        double dLon = Math.toRadians(other.lon - this.lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public String toString() {
        return address;
    }
}
