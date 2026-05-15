package nl.vintageforlife.poc.logic;

import nl.vintageforlife.poc.domain.Deliverer;
import nl.vintageforlife.poc.domain.Location;
import nl.vintageforlife.poc.domain.Order;
import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;
import nl.vintageforlife.poc.domain.Vehicle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Centrale planningsklasse. Verzamelt orders, voertuigen en medewerkers,
 * roept het algoritme aan en bouwt daaruit Route-objecten met Stops.
 * Een planner kan een gegenereerde route hier ook goedkeuren.
 *
 * Bij Vintage for Life rijdt er altijd een tweetal mee: een driver en een
 * assistent. Tijdens het genereren worden deze rollen automatisch aan een
 * route toegewezen op basis van de beschikbare medewerkers.
 */
public class RouteManagement {

    private final DeliveryAlgorithm algorithm = new DeliveryAlgorithm();
    private final List<Order> orders = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Deliverer> deliverers = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private Location depot;
    /** Standaard dagstart in minuten sinds 00:00 (default 08:00). */
    private int dayStartMinutes = 8 * 60;
    private int routeSequence = 1;
    private int stopSequence = 1;

    public void setDepot(Location depot) { this.depot = depot; }
    public void setDayStart(int dayStartMinutes) { this.dayStartMinutes = dayStartMinutes; }

    public List<Order> getOrders() { return orders; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Deliverer> getDeliverers() { return deliverers; }
    public List<Route> getRoutes() { return routes; }

    public void addOrder(Order o) { orders.add(o); }
    public void addVehicle(Vehicle v) { vehicles.add(v); }
    public void addDeliverer(Deliverer d) { deliverers.add(d); }

    /**
     * Genereert routes op basis van alle open orders. Bestaande
     * niet-goedgekeurde routes worden weggegooid en opnieuw berekend.
     * Elke route krijgt automatisch een driver en een assistent toegewezen
     * zolang er genoeg medewerkers beschikbaar zijn.
     */
    public List<Route> generateRoutes() {
        if (depot == null) {
            throw new IllegalStateException("Depot is niet ingesteld.");
        }
        // Verwijder oude concepten; goedgekeurde routes laten we staan.
        routes.removeIf(r -> r.getStatus() == Route.Status.CONCEPT);

        List<Order> openOrders = new ArrayList<>();
        for (Order o : orders) {
            if (!o.isDelivered() && !isInExistingRoute(o)) {
                openOrders.add(o);
            }
        }
        if (openOrders.isEmpty()) return new ArrayList<>();

        DeliveryAlgorithm.RoutingResult result =
                algorithm.solve(vehicles, openOrders, depot, dayStartMinutes);

        List<Route> newRoutes = new ArrayList<>();
        // Iterator over alle beschikbare medewerkers; we koppelen er per route
        // twee aan vast (driver + assistent).
        Iterator<Deliverer> crewCycle = deliverers.iterator();

        for (DeliveryAlgorithm.PlannedRoute pr : result.plannedRoutes) {
            Route route = new Route("R-" + routeSequence++, pr.vehicle, dayStartMinutes);
            route.setTotalDistanceKm(pr.distanceKm);

            // Loop de geplande stops door en bereken ETA's op basis van
            // hemelsbrede afstand en 50 km/u (zie DeliveryAlgorithm).
            int seq = 1;
            int currentTime = dayStartMinutes;
            Location prevLoc = depot;
            for (Order o : pr.orderedOrders) {
                int travel = (int) Math.round(prevLoc.distanceKm(o.getAddress()) / 50.0 * 60.0);
                currentTime += travel;
                if (currentTime < o.getTimeWindowStart()) {
                    currentTime = o.getTimeWindowStart();  // wachten op tijdvenster
                }
                Stop stop = new Stop("S-" + stopSequence++, seq++, o, currentTime);
                route.addStop(stop);
                currentTime += o.getServiceMinutes();
                prevLoc = o.getAddress();
            }

            // Wijs (indien beschikbaar) een driver en assistent toe.
            if (crewCycle.hasNext()) {
                crewCycle.next().assignAsDriver(route);
            }
            if (crewCycle.hasNext()) {
                crewCycle.next().assignAsAssistant(route);
            }
            newRoutes.add(route);
            routes.add(route);
        }
        return newRoutes;
    }

    /** Helper: zit de order al in een actieve (niet-concept) route? */
    private boolean isInExistingRoute(Order o) {
        for (Route r : routes) {
            if (r.getStatus() == Route.Status.CONCEPT) continue;
            for (Stop s : r.getStops()) {
                if (s.getOrder() == o) return true;
            }
        }
        return false;
    }

    /** Een planner keurt een conceptroute goed (zie klassendiagram). */
    public void approveRoute(Route route) {
        if (route.getStatus() == Route.Status.CONCEPT) {
            route.setStatus(Route.Status.GOEDGEKEURD);
        }
    }
}
