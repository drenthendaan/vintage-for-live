package nl.vintageforlife.poc.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * De bezorger. Verantwoordelijk voor de praktische uitvoering van leveringen.
 * Kan zijn toegewezen routes opvragen.
 */
public class Deliverer {
    private final String delivererId;
    private final String name;
    private final List<Route> assignedRoutes = new ArrayList<>();

    public Deliverer(String delivererId, String name) {
        this.delivererId = delivererId;
        this.name = name;
    }

    public String getDelivererId() { return delivererId; }
    public String getName() { return name; }

    public List<Route> getAssignedRoutes() { return assignedRoutes; }

    public void assignRoute(Route route) {
        assignedRoutes.add(route);
        route.addDeliverer(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
