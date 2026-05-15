package nl.vintageforlife.poc.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Een medewerker uit het bezorgteam. Kan als driver (chauffeur) of als
 * assistent (bijrijder) aan een route worden gekoppeld. Bij Vintage for Life
 * rijdt er altijd een tweetal mee: de driver bestuurt het busje en de
 * assistent helpt met sjouwen en installatie.
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

    /** Alle routes waar deze persoon als driver op staat. */
    public List<Route> getDrivenRoutes() { return drivenRoutes; }

    /** Alle routes waar deze persoon als assistent op staat. */
    public List<Route> getAssistedRoutes() { return assistedRoutes; }

    /** Alle routes waar deze persoon bij betrokken is (driver of assistent). */
    public List<Route> getAssignedRoutes() {
        List<Route> all = new ArrayList<>(drivenRoutes);
        for (Route r : assistedRoutes) {
            if (!all.contains(r)) all.add(r);
        }
        return all;
    }

    /** Koppel deze persoon als driver aan de route. */
    public void assignAsDriver(Route route) {
        if (!drivenRoutes.contains(route)) drivenRoutes.add(route);
        route.setDriver(this);
    }

    /** Koppel deze persoon als assistent aan de route. */
    public void assignAsAssistant(Route route) {
        if (!assistedRoutes.contains(route)) assistedRoutes.add(route);
        route.setAssistant(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
