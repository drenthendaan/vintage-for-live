package nl.vintageforlife.poc.domain;

/**
 * Simpele lat/lon locatie. Gebruikt voor depot, voertuig startpunt en
 * orderadressen. In de PoC rekenen we hemelsbreed (haversine); in productie
 * zou GraphHopper hier de echte rijafstanden leveren (zie TO hoofdstuk
 * "Gekozen algoritme").
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

    /** Haversine afstand in kilometers tussen deze locatie en {@code other}. */
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
