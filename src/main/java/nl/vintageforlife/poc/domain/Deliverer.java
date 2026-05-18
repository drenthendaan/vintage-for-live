package nl.vintageforlife.poc.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A member of the delivery crew. Can be linked to a route as a driver or as
 * an assistant. At Vintage for Life every delivery is done by a pair: the
 * driver operates the van and the assistant helps with carrying and
 * installation.
 */
public class Deliverer {
    private final String delivererId;
    private final String name;
    private final List<Route> drivenRoutes = new ArrayList<>();
    private final List<Route> assistedRoutes = new ArrayList<>();

    public Deliverer(String delivererId, String name) {
        this.delivererId = delivererId;
        this.name = name;
    }

    public String getDelivererId() { return delivererId; }
    public String getName() { return name; }

    /** All routes on which this person is the driver. */
    public List<Route> getDrivenRoutes() { return drivenRoutes; }

    /** All routes on which this person is the assistant. */
    public List<Route> getAssistedRoutes() { return assistedRoutes; }

    /** All routes this person is involved in (as driver or assistant). */
    public List<Route> getAssignedRoutes() {
        List<Route> assignedRoutes = new ArrayList<>(drivenRoutes);
        for (Route r : assistedRoutes) {
            if (!assignedRoutes.contains(r)) assignedRoutes.add(r);
        }

        return assignedRoutes;
    }

    /** Assigns this person to the route as the driver. */
    public void assignAsDriver(Route route) {
        if (!drivenRoutes.contains(route)) drivenRoutes.add(route);
        route.setDriver(this);
    }

    /** Assigns this person to the route as the assistant. */
    public void assignAsAssistant(Route route) {
        if (!assistedRoutes.contains(route)) assistedRoutes.add(route);
        route.setAssistant(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
